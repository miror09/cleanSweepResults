/**
 * 
 */
package cleanSweepResults;

import com.filenet.api.core.Document;
import com.filenet.api.core.IndependentlyPersistableObject;

/**
 * @author Administrator
 *
 */
public class CleanSweepResults {

	/**
	 * This method is called for every selected object. The signature of the
	 * method should not be changed. The name of the method and the rest of the
	 * code can be adjusted to your needs.
	 * 
	 * @param object
	 *            the selected object
	 * @throws Exception
	 *             the exception. Exceptions are displayed in the console and
	 *             does not stop the execution of the rest of the selected
	 *             objects.
	 */
	public void run(Object object) throws Exception {
		IndependentlyPersistableObject independentlyPersistableObject = (IndependentlyPersistableObject) object;

		// TODO Auto-generated method stub 
		System.out.println("Start");
		Document document = (Document) object;
		System.out.println(document.get_Name());
		System.out.println("Stop");

	}

}
