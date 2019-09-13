job "battery-measurments-monitor" {

  datacenters = ["nbg1-dc3"]

  type = "service"

  constraint {
    attribute = "${node.unique.name}"
    value     = "nomad-client-1"
  }

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

      # Specifies what should be executed when starting the job
      config {
        jar_path    = "local/battery-measurements-api.jar"
      }

      # Defines the source of the artifact which should be downloaded
      artifact {
        source = "https://battery-measurements-api-deployments-staging.s3.eu-central-1.amazonaws.com/${CI_COMMIT_SHORT_SHA}/battery-measurements-api.jar"
        destination = "local/"
      }

      # The service block tells Nomad how to register this service with Consul for service discovery and monitoring.
      service {
        name = "battery-measurements-monitor"
        port = "http"

        tags = ["production"]

        check {
          type = "http"
          path = "/health"
          interval = "2m"
          timeout = "5m"
        }
      }

      # Specify the maximum resources required to run the job, include CPU, and memory
      resources {
        cpu = 2000 # Mhz
        memory = 1024 # MB

        network {
          port "http" {
            static = "8080"
          }
        }
      }

      env {
        DATABASE_URL = "${PRODUCTION_DATABASE_URL}"
        PORT = "8080"
        DATADOG_AGENT_URL = "116.203.80.78:8125"
        SENTRY_URL = "https://185dca28f02c4334b30177c5b14f2193:4db048a7a8c641488d7554ac2d550cc5@sentry.sonnenbatterie.de/54"
      }
    }
  }
}
