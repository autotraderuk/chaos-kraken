# Chaos Kraken

Application to simulate JVM based failure scenarios when running on a delivery platform.

> Terrorise the Shipp'ng lanes with the Chaos Kraken, an incomplete FAAS (Failures as a Service).  

The first version of Chaos Kraken was created at [Auto Trader UK](https://careers.autotrader.co.uk/) back in 2017. It was originally built to test various application failure conditions on their private cloud infrastructure.

As Auto Trader started their migration to public cloud and Kubernetes, the Chaos Kraken evolved to cater for more failure modes.

Chaos Kraken is actively used within Auto Trader to verify various behaviours of their [GKE](https://cloud.google.com/kubernetes-engine) based delivery platform. 

## Usage

### Start-up

Simply set the `FAIL_ON_START` environment variable to one of the failure types. 

 ```bash
FAIL_ON_START=killapp ./gradlew clean bootRun 
```

### Runtime

Send a `POST` request to your desired failure e.g. 

```bash
curl -X POST http://localhost:8080/simulate/memoryleak`
```

To have Monkeynetes return a desired status code, send a `GET` request to `/echostatus/{CODE}` e.g. 

```bash
curl http://localhost:8080/echostatus/403
```

## Failures

- `toggle-service-health`

  Toggle application health check between healthy and unhealthy.

- `unhealthy-service`

  Toggle application health check to return unhealthy status. It will be unhealthy forever.

- `memoryleak`

  Start allocating memory until error, catch the `OutOfMemoryError`.
  
- `memoryleak-oom`

  Start allocating memory until error, uncaught `OutOfMemoryError`.

- `wastecpu`

  Start hashing random strings forever.

- `threadbomb`

  Spin up an infinite amount of threads that never end.

- `filehandlebomb`

  Start opening random new files and never closing them.

- `filewriter`

  Start writing random 1KB files to `java.io.tmpdir`.

- `stdoutbomb`

  Start repeatedly writing timestamps to `stdout`.
  
  Accepts `periodMillis` as a query parameter to set the rate (defaults to 1ms)

- `diskbomb`

  Start writing random 1GB files to random locations on disk.

- `killapp`

  Emit a log message and then immediately terminate the JVM.

- `selfconnectionsbomb`

  Start Listening on a random local port and open 5000 connections to that port

- `directmemoryleak`

  Start allocating direct memory until error.

  Accepts `limitMB` as a query parameter to set the amount of memory to leak (default is no limit, ie until OOM)
