package example.users;

import org.junit.Assert;
import org.junit.Test;

public class SumaTest {
	
	@Test
	public void sumaTest() {
		Suma miSuma = new Suma(1 , 1);
		Assert.assertEquals(new int(1), miSuma.suma());
	}

}
