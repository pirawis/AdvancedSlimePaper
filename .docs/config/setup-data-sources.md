# Data Source Configuration

This guide explains how to configure the various data sources available in AdvancedSlimePaper.

## Configuration File

Data sources are configured in:
```
plugins/SlimeWorldManager/sources.yml
```

## File System (Default)

The file system loader is always available and stores worlds as `.slime` files.

```yaml
file:
  path: slime_worlds
```

| Option | Description | Default |
|--------|-------------|---------|
| `path` | Directory for world files (relative to plugin folder) | `slime_worlds` |

World files are stored at:
```
plugins/SlimeWorldManager/slime_worlds/<worldname>.slime
```

---

## MySQL

Store worlds in a MySQL or MariaDB database. Ideal for multi-server setups.

```yaml
mysql:
  enabled: true
  host: 127.0.0.1
  port: 3306
  username: slimeworldmanager
  password: your_password
  database: slimeworldmanager
  usessl: false
```

| Option | Description | Default |
|--------|-------------|---------|
| `enabled` | Enable MySQL loader | `false` |
| `host` | Database server address | `127.0.0.1` |
| `port` | Database port | `3306` |
| `username` | Database username | - |
| `password` | Database password | - |
| `database` | Database name | `slimeworldmanager` |
| `usessl` | Use SSL connection | `false` |

### Database Setup

The plugin automatically creates the required table:

```sql
CREATE TABLE IF NOT EXISTS `worlds` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(255) UNIQUE NOT NULL,
  `world` MEDIUMBLOB NOT NULL,
  `locked` TINYINT(1) NOT NULL DEFAULT 0
);
```

### Connection Pool

MySQL uses HikariCP for connection pooling with optimized settings.

---

## MongoDB

Store worlds in MongoDB. Good for scalable deployments.

```yaml
mongodb:
  enabled: true
  host: 127.0.0.1
  port: 27017
  username: slimeworldmanager
  password: your_password
  auth: admin
  database: slimeworldmanager
  collection: worlds
  uri: ''
```

| Option | Description | Default |
|--------|-------------|---------|
| `enabled` | Enable MongoDB loader | `false` |
| `host` | MongoDB server address | `127.0.0.1` |
| `port` | MongoDB port | `27017` |
| `username` | MongoDB username | - |
| `password` | MongoDB password | - |
| `auth` | Authentication database | `admin` |
| `database` | Database name | `slimeworldmanager` |
| `collection` | Collection name | `worlds` |
| `uri` | Full connection URI (overrides other settings) | - |

### Using Connection URI

For complex configurations (replica sets, Atlas, etc.), use the `uri` option:

```yaml
mongodb:
  enabled: true
  uri: 'mongodb+srv://user:password@cluster.mongodb.net/slimeworldmanager?retryWrites=true&w=majority'
  database: slimeworldmanager
  collection: worlds
```

---

## Redis

Store worlds in Redis for high-performance access.

```yaml
redis:
  enabled: true
  uri: 'redis://localhost:6379'
```

| Option | Description | Default |
|--------|-------------|---------|
| `enabled` | Enable Redis loader | `false` |
| `uri` | Redis connection URI | - |

### URI Examples

```yaml
# Local Redis
uri: 'redis://localhost:6379'

# With password
uri: 'redis://:password@localhost:6379'

# With database number
uri: 'redis://localhost:6379/1'

# Redis Sentinel
uri: 'redis-sentinel://localhost:26379/0#mymaster'

# Redis Cluster
uri: 'redis://node1:6379,node2:6379,node3:6379'
```

---

## Remote API

Connect to a custom HTTP API for world storage.

```yaml
api:
  enabled: true
  url: 'https://your-api.example.com'
  username: ''
  token: 'your-api-token'
  ignoreSslCertificate: false
```

| Option | Description | Default |
|--------|-------------|---------|
| `enabled` | Enable API loader | `false` |
| `url` | API base URL | - |
| `username` | API username | - |
| `token` | API authentication token | - |
| `ignoreSslCertificate` | Skip SSL verification (not recommended) | `false` |

### API Implementation

Your API should implement the following endpoints:

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/worlds` | List all world names |
| `GET` | `/worlds/{name}` | Get world data |
| `POST` | `/worlds/{name}` | Save world data |
| `DELETE` | `/worlds/{name}` | Delete a world |
| `HEAD` | `/worlds/{name}` | Check if world exists |

---

## Complete Example

```yaml
# File system - always available
file:
  path: slime_worlds

# MySQL - for multi-server setups
mysql:
  enabled: true
  host: db.example.com
  port: 3306
  username: minecraft
  password: secure_password_here
  database: slimeworldmanager
  usessl: true

# MongoDB - for scalable deployments
mongodb:
  enabled: true
  uri: 'mongodb+srv://minecraft:password@cluster.mongodb.net/?retryWrites=true'
  database: slimeworldmanager
  collection: worlds

# Redis - for high-performance caching
redis:
  enabled: false
  uri: 'redis://localhost:6379'

# Remote API - for custom infrastructure
api:
  enabled: false
  url: 'https://api.example.com'
  token: 'your-token'
  ignoreSslCertificate: false
```

## Choosing a Data Source

| Use Case | Recommended Source |
|----------|-------------------|
| Single server | `file` |
| Multi-server (BungeeCord/Velocity) | `mysql` or `mongodb` |
| High-performance requirements | `redis` |
| Scalable cloud deployment | `mongodb` |
| Custom infrastructure | `api` |
| Development/testing | `file` |

## Security Notes

1. **Never commit credentials**: Use environment variables or separate config files
2. **Use SSL**: Enable SSL for database connections in production
3. **Restrict access**: Limit database user permissions to only what's needed
4. **Firewall**: Restrict database access to your server IPs only
5. **Strong passwords**: Use randomly generated passwords for all services
