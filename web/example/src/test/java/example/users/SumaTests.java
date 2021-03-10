package example.users;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;


public class SumaTests {
	
	@Test
	public void sumaTest() {
		Suma miSuma = new(1,1);
		Assert.assertEquals(new int(1),miSuma.suma());
	}
}
