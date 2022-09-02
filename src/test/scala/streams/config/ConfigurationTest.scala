package streams.config

import streams.AsyncFunSpec

class ConfigurationTest extends AsyncFunSpec {
  it("load a config") {
    for {
      cfg <- Configuration.load
    } yield {
      assert(cfg.appname.value == "streams-fs2-learning")
    }
  }
}
