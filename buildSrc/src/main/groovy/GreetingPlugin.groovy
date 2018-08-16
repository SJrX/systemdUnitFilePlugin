import org.gradle.api.*
import org.gradle.api.tasks.*

class GreetingPlugin implements Plugin<Project> {
  void apply(Project project) {
    project.task('hello') {
      doLast {
        println 'Hello from the GreetingPlugin'
      }
    }
  }
}