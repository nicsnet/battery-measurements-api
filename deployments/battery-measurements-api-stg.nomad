job "battery-measurments-api-staging" {

  datacenters = ["nbg1-dc3"]

  type = "service"

  group "server" {
    count = 1

    restart {
      # The number of attempts to run the job within the specified interval.
      attempts = 2
      interval = "5m"
      delay = "1m"
      mode = "delay"
    }

    update {
      max_parallel = 1

      # Enable automatically reverting to the last stable job on a failed
      # deployment.
      auto_revert = true
    }

    # Create an individual task (unit of work)
    task "app" {
      driver = "java"

      # Specifies what should be executed when starting the jobG
      config {
        jar_path  = "local/battery-measurements-api.jar"
      }

      # Defines the source of the artifact which should be downloaded
      artifact {
        source = "https://battery-measurements-api-deployments-staging.s3.eu-central-1.amazonaws.com/${CI_COMMIT_SHORT_SHA}/battery-measurements-api.jar"
        destination = "local/"
      }

      # The service block tells Nomad how to register this service with Consul for service discovery and monitoring.
      service {
        name = "battery-measurements-api-staging"
        port = "http"

        tags = ["staging"]

        check {
          type = "http"
          path = "/health"
          interval = "2m"
          timeout = "5m"
        }
      }

      # Specify the maximum resources required to run the job, include CPU, and memory
      resources {
        cpu = 1000 # Mhz
        memory = 1024 # MB

        network {
          port "http" {
            static = "8080"
          }
        }
      }

      env {
        DATABASE_URL = "${STAGING_DATABASE_URL}"
        PORT = "8080"
        DATADOG_AGENT_URL = "no-agent"
        SENTRY_URL = "https://95e9c5593fe44102bb620689774772fe:0cd4fa17b99948ea8ca4971f6927a0f1@sentry.sonnenbatterie.de/55"
      }
    }
  }
}
