# Grid enhancements collection add-on

This add-on contains enhancements to Grid, but also has other
grid enhancement libraries included as submodules.

## Cloning the project

When cloning the project use the command:

    git clone --recursive [url of the repository]

or

    git clone [url of the repository]

    git submodule update --init

or

    git clone [url of the repository]

    git submodule init

    git submodule update

## More about submodules

https://git-scm.com/book/en/v2/Git-Tools-Submodules

## Contained extension add-on projects

- grid-renderers-collection-addon
- gridactionrenderer-addon
- GridExtensionPack

## Building the project

mvn clean install
- running the demo

cd GridEnhancements/GirdEnhancements-demo

mvn jetty:run
