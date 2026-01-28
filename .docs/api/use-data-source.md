# Custom Data Sources

AdvancedSlimePaper supports multiple data sources out of the box: file system, MySQL, MongoDB, Redis, and remote API. However, you can create custom data sources by implementing the `SlimeLoader` interface.

## Built-in Data Sources

| Loader | Description | Best For |
|--------|-------------|----------|
| `file` | Local file system (.slime files) | Single-server setups |
| `mysql` | MySQL/MariaDB database | Multi-server with existing MySQL |
| `mongodb` | MongoDB database | Scalable deployments |
| `redis` | Redis in-memory store | High-performance caching |
| `api` | Remote HTTP API | Custom infrastructure |

## Getting a Loader

```java
SlimePlugin plugin = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");

SlimeLoader fileLoader = plugin.getLoader("file");
SlimeLoader mysqlLoader = plugin.getLoader("mysql");
SlimeLoader mongoLoader = plugin.getLoader("mongodb");
SlimeLoader redisLoader = plugin.getLoader("redis");
SlimeLoader apiLoader = plugin.getLoader("api");
```

## Creating a Custom SlimeLoader

Implement the `SlimeLoader` interface to create your own data source:

```java
import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.api.exceptions.UnknownWorldException;

public class MyCustomLoader implements SlimeLoader {

    @Override
    public byte[] readWorld(String worldName) throws UnknownWorldException, IOException {
        // Read the world's serialized data from your data source
        // Return the raw bytes of the .slime file
        byte[] data = myDataSource.get(worldName);
        if (data == null) {
            throw new UnknownWorldException(worldName);
        }
        return data;
    }

    @Override
    public boolean worldExists(String worldName) throws IOException {
        // Check if a world exists in your data source
        return myDataSource.contains(worldName);
    }

    @Override
    public List<String> listWorlds() throws IOException {
        // Return a list of all world names in your data source
        return myDataSource.getAllKeys();
    }

    @Override
    public void saveWorld(String worldName, byte[] serializedWorld) throws IOException {
        // Save the serialized world data to your data source
        myDataSource.put(worldName, serializedWorld);
    }

    @Override
    public void deleteWorld(String worldName) throws UnknownWorldException, IOException {
        // Delete a world from your data source
        if (!worldExists(worldName)) {
            throw new UnknownWorldException(worldName);
        }
        myDataSource.delete(worldName);
    }
}
```

## Registering a Custom Loader

After implementing your loader, register it with the plugin:

```java
SlimePlugin plugin = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");

MyCustomLoader customLoader = new MyCustomLoader();
plugin.registerLoader("my_custom_source", customLoader);

// Now you can use it
SlimeLoader loader = plugin.getLoader("my_custom_source");
```

## Example: S3 Storage Loader

Here's an example of a custom loader for Amazon S3:

```java
public class S3SlimeLoader implements SlimeLoader {

    private final S3Client s3Client;
    private final String bucketName;

    public S3SlimeLoader(S3Client s3Client, String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    @Override
    public byte[] readWorld(String worldName) throws UnknownWorldException, IOException {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(worldName + ".slime")
                .build();

            ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(request);
            return response.asByteArray();
        } catch (NoSuchKeyException e) {
            throw new UnknownWorldException(worldName);
        } catch (S3Exception e) {
            throw new IOException("Failed to read from S3", e);
        }
    }

    @Override
    public boolean worldExists(String worldName) throws IOException {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(worldName + ".slime")
                .build();
            s3Client.headObject(request);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            throw new IOException("Failed to check S3", e);
        }
    }

    @Override
    public List<String> listWorlds() throws IOException {
        try {
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build();

            return s3Client.listObjectsV2(request).contents().stream()
                .map(S3Object::key)
                .filter(key -> key.endsWith(".slime"))
                .map(key -> key.replace(".slime", ""))
                .collect(Collectors.toList());
        } catch (S3Exception e) {
            throw new IOException("Failed to list S3 objects", e);
        }
    }

    @Override
    public void saveWorld(String worldName, byte[] serializedWorld) throws IOException {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(worldName + ".slime")
                .build();

            s3Client.putObject(request, RequestBody.fromBytes(serializedWorld));
        } catch (S3Exception e) {
            throw new IOException("Failed to write to S3", e);
        }
    }

    @Override
    public void deleteWorld(String worldName) throws UnknownWorldException, IOException {
        if (!worldExists(worldName)) {
            throw new UnknownWorldException(worldName);
        }

        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(worldName + ".slime")
                .build();

            s3Client.deleteObject(request);
        } catch (S3Exception e) {
            throw new IOException("Failed to delete from S3", e);
        }
    }
}
```

## SlimeLoader Interface Reference

| Method | Description |
|--------|-------------|
| `readWorld(String)` | Read world data as byte array |
| `worldExists(String)` | Check if world exists |
| `listWorlds()` | List all world names |
| `saveWorld(String, byte[])` | Save world data |
| `deleteWorld(String)` | Delete a world |

## Best Practices

1. **Thread Safety**: Ensure your loader is thread-safe for async operations
2. **Connection Pooling**: Use connection pools for database loaders
3. **Error Handling**: Throw appropriate exceptions (`UnknownWorldException`, `IOException`)
4. **Locking**: Consider implementing world locking for multi-server setups
5. **Compression**: World data is already compressed; no need to compress again
6. **Caching**: Consider caching for frequently accessed worlds

## Source Code Reference

- [SlimeLoader.java](../../api/src/main/java/com/infernalsuite/asp/api/loaders/SlimeLoader.java) - Interface definition
- [FileLoader.java](../../loaders/file-loader/src/main/java/com/infernalsuite/asp/loaders/file/FileLoader.java) - File system implementation
- [MysqlLoader.java](../../loaders/mysql-loader/src/main/java/com/infernalsuite/asp/loaders/mysql/MysqlLoader.java) - MySQL implementation
