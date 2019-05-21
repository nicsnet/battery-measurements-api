# battery-measurements-api


The battery measurements api is a service extracted out of the Core api. It is written in Clojure using the [Luminus web framework](http://luminusweb.net/)





## Prerequisites

You will need [Leiningen](https://github.com/technomancy/leiningen] 2.0 or above installed, as well as [Docker](https://www.docker.com/get-docker)


[1]: https://github.com/technomancy/leiningen

## Running the application with docker

 - Run `docker-compose up --build battery-measurements-api` to get the service running
 - Then you can access the service via that Swagger UI at [http://localhost:3006/havel/api-docs/index.html](http://localhost:3006/havel/api-docs/index.html)


### Running the battery-measurements-api outside of docker with the provided mysql
For normal development:
 1. Run `docker-compose up -d db`
 2. Run `lein repl`. This can take a few minutes the first time.
 3. When you see `user=>`, you have a running prompt. Run `(start)`
 4. Now the service should be up and running at [http://localhost:3000](http://localhost:3000)
 5. You can connect to the nREPL with the editor of your choice:
     - In vim, you can use [Vim Fireplace](https://github.com/tpope/vim-fireplace) to `:Connect` to the nREPL port
     - In emacs/spacemacs you can use `M-x cider-connect` assuming you have cider installed (with spacemacs, enable the `clojure` layer and you can use the shortcut `, s c`)
     - In IntelliJ with Cursive you can connect with a Remote REPL using a Leiningen REPL Port (see [the Cursive REPL documentation](https://cursive-ide.com/userguide/images/remote-repl-config.png))
     - You can also type things into the same terminal window you typed `(start)` in, but this can be less powerful.
