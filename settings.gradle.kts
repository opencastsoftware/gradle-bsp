rootProject.name = "gradle-bsp"

include("model")
project(":model").name = "gradle-bsp-model"
include("plugin")
project(":plugin").name = "gradle-bsp-plugin"
include("server")
project(":server").name = "gradle-bsp-server"
