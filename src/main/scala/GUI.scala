import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.{Parent, Scene}
import javafx.stage.Stage

class GUI extends Application{

  override def start(primaryStage: Stage): Unit = {

    primaryStage.setTitle("Konane App")

    val FXMLLoader = new FXMLLoader(getClass.getResource("controller.fxml"))
    val mainViewRoot: Parent = FXMLLoader.load()

    val scene = new Scene(mainViewRoot)
    primaryStage.setScene(scene)
    primaryStage.show()
  }
}
object FxApp {
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[GUI], args:_*)
  }
}
