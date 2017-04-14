package org.spongepowered.common.resource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.resource.Resource;
import org.spongepowered.api.resource.ResourceLoader;
import org.spongepowered.api.resource.ResourceLocation;
import org.spongepowered.common.util.persistence.JsonTranslator;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

import javax.annotation.Nullable;

public class FileResourceLoader implements ResourceLoader {

    private final FileSystem fs;

    public FileResourceLoader(Path base) {
        fs = FileSystems.getFileSystem(base.toUri());
    }

    @Override
    public Optional<Resource> getResource(ResourceLocation location) {
        Path path = getPath(location);
        if (Files.exists(path)) {
            return Optional.of(new Resource() {

                @Nullable DataView meta;

                @Override
                public ResourceLocation getResourceLocation() {
                    return location;
                }

                @Override
                public InputStream getInputStream() throws IOException {
                    return Files.newInputStream(path, StandardOpenOption.READ);
                    // close the zip file when the input stream is closed
                }

                @Override
                public ResourceLoader getResourceLoader() {
                    return FileResourceLoader.this;
                }

                @Override
                public Optional<DataView> getMetadata() {
                    if (meta == null) {
                        Path metaPath = path.resolveSibling(path.getFileName() + ".mcmeta");
                        if (Files.exists(metaPath)) {
                            try (InputStream in = Files.newInputStream(metaPath, StandardOpenOption.READ)) {
                                JsonObject obj = new Gson().fromJson(new InputStreamReader(in), JsonObject.class);
                                meta = JsonTranslator.translateFrom(obj);
                                // TODO SpongeAPI#1363
                                // meta = DataFormats.JSON.readFrom(in);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    return Optional.ofNullable(meta);
                }
            });
        }
        return Optional.empty();
    }

    private Path getPath(ResourceLocation loc) {
        return fs.getPath("assets", loc.getDomain(), loc.getPath());
    }

}
