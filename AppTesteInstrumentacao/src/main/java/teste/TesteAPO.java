package teste;

import br.ufrn.ppgsc.scenario.analyzer.common.annotations.arq.Scenario;

public class TesteAPO {

	@Scenario(name = "teste local")
	public void imprimir() {
		System.out.println("Teste metodo");
	}

}