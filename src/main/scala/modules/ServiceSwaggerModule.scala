package modules

import com.google.inject.Provides
import com.jakehschwartz.finatra.swagger.SwaggerModule
import io.swagger.models.{Contact, Info, Swagger}
import io.swagger.models.auth.BasicAuthDefinition

object ServiceSwaggerModule extends SwaggerModule {
  val swaggerUI      = new Swagger()
  val serviceVersion = flag[String]("service.version", "NA", "the version of service")

  @Provides
  def swagger: Swagger = {

    val info = new Info()
      .contact(new Contact().name("Chenghsien Wen").email("chenghsien_wen@htc.com"))
      .description("**mwc-promote-service** - mwc promote service for supporting game event.")
      .version(serviceVersion())
      .title("mwc-promote-service API")

    swaggerUI
      .info(info)
      .addSecurityDefinition("sampleBasic", {
        val d = new BasicAuthDefinition()
        d.setType("basic")
        d
      })

    swaggerUI
  }
}
