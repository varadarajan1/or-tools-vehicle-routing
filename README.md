# Vehicle-Routing-Solver

This is an experimental project for trying out [Google OR Tools](https://developers.google.com/optimization) library.

To run the project,

`./gradlew clean build run`


There is a single class that loads ride requests from
SymphonyRequest.json from resources, converts into a format suitable for OR Tools
and executes.

Also, make sure to set Google API key in Solver class.
`GeoApiContext context = new GeoApiContext.Builder().apiKey(SET_API_KEY).build(); `


**TODO**
1) Add time window constraints
2) Test out other first solution strategies


**Quick Help Links**

1. [Vehicle Routing with Time Windows](https://developers.google.com/optimization/routing/vrptw)
2. [Github Discussions - OR Tools](https://github.com/google/or-tools/discussions/categories/routing-questions?page=3)

