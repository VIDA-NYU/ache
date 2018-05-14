## ACHE Dashboard

This sub-project contains the front-end for ACHE. It is an
[React](https://reactjs.org/) application created using
[create-react-app](https://github.com/facebook/create-react-app).
A detailed README for using create-react-app is locate at
[README-create-react-app](https://github.com/ViDA-NYU/ache/blob/master/ache-dashboard/README-create-react-app.md).


### Building in Production Mode

This project is configured to be automatically compiled in production mode
during the main project build. It will also be automatically packaged inside
the main ACHE JAR files.

### Running in Develoment Mode

To run ache-dashboard interface in development mode, use:

    npm start

This command will start a web server at http://localhost:3000. Every time you
modify the source code files of this project, the browser will reload
automatically. `ache-dashboard` requires a ACHE REST API to communicate with,
so you will also need to run ACHE on port 8080 manually:

    ache startServer -d /path-to-data-storage/

Alternatively, if you have ACHE running in a different address, you can
configure it in the `package.json` file (located in the root of this
sub-project), by modifying the line that contains:

    "proxy": "http://localhost:8080"


#### Skipping ache-dashboard's build

Every time you build the main ACHE application, this project will also be fully
compiled in production mode (which may take a few minues).
If you didn't modify files from this sub-project and want to skip compilation
of `ache-dashboard` to speed-up the build process, you can run Gradle using
the `-x` parameter:

    ./gradlew installDist -x ache-dashboard:build
