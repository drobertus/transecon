import com.mechzombie.transecon.ModelLoader
import com.mechzombie.transecon.actors.Registry
import com.mechzombie.transecon.resources.Bank
import groovy.json.JsonOutput
import spock.lang.Specification

class ModelLoaderSpec extends Specification {

  def file = new File(getClass().getResource('/test_models/test-system-1.json').toURI())
  def reg = Registry.instance

  def setup() {
    reg.cleanup()
  }
  def "load the mode from a file" () {

    setup: "get a path to a file"
    println("file ${file.toString()}")
    def ml = new ModelLoader()

    when: "the file is read"
      def econModel = ml.loadFile(file.toString())

    then: "the model stores 2 households"
    assert econModel != null
    assert econModel.startCondition.households.size() == 2

    when: "the output is loaded"
    JsonOutput out = new JsonOutput()
    println out.toJson(econModel)
    ml.createModel(econModel)

    then: "the actors should be up a spinning"
    reg.households.size() == econModel.startCondition.households.size()
    reg.getActorByModelId(1).getBankBalance() == econModel.startCondition.households[0].bankAccountValue
  }
}
