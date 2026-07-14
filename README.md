# tosan-s3client-spring-boot-starter

A Spring Boot starter for the [AWS SDK S3 client](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/examples-s3.html). Provides auto-configuration for an `S3Client` bean with Micrometer observation support, structured JSON logging, and the Apache HTTP 5 client.

## Features

- Auto-configured `S3Client` bean, ready for injection
- Works with **Amazon S3** and **S3-compatible storage** (MinIO, SeaweedFS, etc.)
- Apache HTTP 5 client with configurable connection pooling and timeouts
- Micrometer observation and OpenTelemetry tracing integration
- Structured JSON logging of S3 operations (invocation, duration, status, request ID)
- Credential handling via access key/secret key or the AWS default credentials chain

## Requirements

- Java 17+
- Spring Boot 4.1+

## Installation

Add the starter dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.tosan.client.s3</groupId>
    <artifactId>tosan-s3client-spring-boot-starter</artifactId>
    <version>latest-version</version>
</dependency>
```

## Configuration

### Amazon S3

```properties
s3.region=us-east-1
s3.access-key=
s3.secret-key=
```

### S3-Compatible Storage (MinIO / SeaweedFS)

```properties
s3.region=us-east-1
s3.base-service-url=http://localhost:9000
s3.access-key=minioadmin
s3.secret-key=minioadmin
```

| Property | Required | Description |
|---|---|---|
| `s3.region` | Yes | AWS region (for S3-compatible storage, any non-empty value) |
| `s3.base-service-url` | No | Base URL for S3-compatible storage (e.g., `http://localhost:9000`) |
| `s3.access-key` | No | AWS access key (falls back to default credentials chain) |
| `s3.secret-key` | No | AWS secret key |
| `s3.http-client.*` | No | HTTP client tuning properties (see table above) |

When `s3.base-service-url` is set, the client enables path-style access and overrides the endpoint. The region is required but used only for request signing — any non-empty value works.

### HTTP Client

```properties
s3.http-client.max-connections=100
s3.http-client.connection-timeout=10s
s3.http-client.socket-timeout=20s
s3.http-client.connection-acquisition-timeout=10s
```

| Property | Default | Description |
|---|---|---|
| `s3.http-client.max-connections` | `100` | Maximum HTTP connections in the pool |
| `s3.http-client.connection-timeout` | `10s` | Timeout for establishing a connection |
| `s3.http-client.socket-timeout` | `20s` | Timeout for socket read/write |
| `s3.http-client.connection-acquisition-timeout` | `10s` | Timeout for acquiring a connection from the pool |


## Usage

Inject the auto-configured `S3Client` bean into your components:

```java
@Service
public class StorageService {

    private final S3Client s3Client;

    public StorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String upload(String bucket, byte[] data) {
        String key = UUID.randomUUID().toString();
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(MediaType.IMAGE_JPEG_VALUE)
                .contentLength((long) data.length)
                .build();
        s3Client.putObject(request, RequestBody.fromBytes(data));
        return key;
    }

    public byte[] download(String bucket, String key) throws IOException {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        try (ResponseInputStream<GetObjectResponse> response = s3Client.getObject(request)) {
            return response.readAllBytes();
        }
    }

    public void delete(String bucket, String key) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        s3Client.deleteObject(request);
    }
}
```

## Customization

All auto-configured beans use `@ConditionalOnMissingBean`, so you can override any of them by defining your own:

- `S3Client` — the S3 client
- `s3HttpClient` — the `SdkHttpClient` used for HTTP transport
- `s3ObservationInterceptor` — the `ExecutionInterceptor` for observation/logging

## Observability

Each S3 operation produces a Micrometer observation named `s3.sdk_call_service` with the following tags:

| Tag | Cardinality | Description |
|---|---|---|
| `s3.service` | Low | Service name (`s3`) |
| `s3.operation` | Low | Operation name (e.g., `PutObject`, `GetObject`) |
| `s3.bucket` | Low | Target bucket name |
| `s3.status_code` | Low | HTTP status code |
| `s3.request_id` | High | AWS/S3 request ID |

Structured JSON log entries are emitted at `INFO` level for successful calls and `ERROR` level for failures, including duration, status, and request ID.

## Sample Application

A sample application is included in the `tosan-s3client-spring-boot-sample` module. Configure `application.properties` with your S3 credentials, then run:

```bash
mvn spring-boot:run -pl tosan-s3client-spring-boot-sample
```

## License

Apache License 2.0
