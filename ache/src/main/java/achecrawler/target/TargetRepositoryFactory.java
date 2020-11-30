package achecrawler.target;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import achecrawler.target.repository.ElasticSearchRestTargetRepository;
import achecrawler.target.repository.FileSystemTargetRepository;
import achecrawler.target.repository.FilesTargetRepository;
import achecrawler.target.repository.MultipleTargetRepositories;
import achecrawler.target.repository.RocksDBTargetRepository;
import achecrawler.target.repository.TargetRepository;
import achecrawler.target.repository.WarcTargetRepository;
import achecrawler.target.repository.FileSystemTargetRepository.DataFormat;
import achecrawler.target.repository.elasticsearch.ElasticSearchConfig;
import achecrawler.target.repository.kafka.KafkaTargetRepository;

public class TargetRepositoryFactory {

    public static final Logger logger = LoggerFactory.getLogger(TargetRepositoryFactory.class);

    public static TargetRepository create(String dataPath, String esIndexName,
            String esTypeName, TargetStorageConfig config) throws IOException {
        List<String> dataFormats = config.getDataFormats();

        List<TargetRepository> repositories = new ArrayList<>();
        for (String dataFormat : dataFormats) {
            TargetRepository targetRepository =
                    createRepository(dataFormat, dataPath, esIndexName, esTypeName, config);
            repositories.add(targetRepository);
        }

        TargetRepository targetRepository = null;
        if (repositories.size() == 1) {
            targetRepository = repositories.get(0);
        } else if (repositories.size() > 1) {
            // create pool of repositories
            targetRepository = new MultipleTargetRepositories(repositories);
        } else {
            throw new IllegalArgumentException("No valid data formats configured.");
        }
        return targetRepository;
    }

    private static TargetRepository createRepository(String dataFormat, String dataPath,
            String esIndexName, String esTypeName, TargetStorageConfig config) throws IOException {

        Path targetDirectory = Paths.get(dataPath, config.getTargetStorageDirectory());
        boolean compressData = config.getCompressData();
        boolean hashFilename = config.getHashFileName();

        logger.info("Loading repository with data_format={} from {}",
                dataFormat, targetDirectory.toString());

        switch (dataFormat) {
            case "ROCKSDB":
                return new RocksDBTargetRepository(targetDirectory);
            case "FILES":
                return new FilesTargetRepository(targetDirectory, config.getMaxFileSize());
            case "FILESYSTEM_JSON":
                return new FileSystemTargetRepository(targetDirectory, DataFormat.JSON,
                        hashFilename, compressData);
            case "FILESYSTEM_CBOR":
                return new FileSystemTargetRepository(targetDirectory, DataFormat.CBOR,
                        hashFilename, compressData);
            case "FILESYSTEM_HTML":
                return new FileSystemTargetRepository(targetDirectory, DataFormat.HTML,
                        hashFilename, compressData);
            case "WARC":
                return new WarcTargetRepository(targetDirectory, config.getWarcMaxFileSize(),
                        config.getCompressWarc());
            case "KAFKA":
                return new KafkaTargetRepository(config.getKafkaConfig());
            case "ELASTICSEARCH":
                ElasticSearchConfig esconfig = config.getElasticSearchConfig();
                if (esIndexName != null && !esIndexName.isEmpty()) {
                    esconfig.setIndexName(esIndexName);
                }
                if (esTypeName != null && !esTypeName.isEmpty()) {
                    esconfig.setTypeName(esTypeName);
                }
                return new ElasticSearchRestTargetRepository(esconfig);

            default:
                throw new IllegalArgumentException("Invalid data format provided: " + dataFormat);
        }
    }

}
