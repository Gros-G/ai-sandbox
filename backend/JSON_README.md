This module forces JSON as the default response format.

- `application.properties` and `WebConfig` configure content negotiation to prefer JSON.
- `/randomQuantumNumber/singleGeneration` returns a JSON payload {"value":..., "timestamp":...}.
- `/randomQuantumNumber/stream` provides SSE events whose data is a JSON payload with the same shape.

To test once the app is running:

curl http://localhost:8080/sandbox/randomQuantumNumber/singleGeneration
curl --no-buffer http://localhost:8080/sandbox/randomQuantumNumber/stream?frequency=2

