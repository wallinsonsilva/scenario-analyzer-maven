package br.ufrn.ppgsc.scenario.analyzer.cdynamic.db;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.List;

import br.ufrn.ppgsc.scenario.analyzer.cdynamic.model.RuntimeScenario;
import br.ufrn.ppgsc.scenario.analyzer.cdynamic.util.RuntimeCallGraphPrintUtil;

public class GenericDAOFileImpl<T extends Serializable> implements GenericDAO<T> {

	private static int count = 0;

	public void clearSession() {

	}

	public T save(T instance) {
		RuntimeScenario rs = (RuntimeScenario) instance;

		try {
			PrintStream ps = new PrintStream("scenario_" + ++count +
					"_" + rs.getRoot().getMemberSignature() + ".txt");
			RuntimeCallGraphPrintUtil.logScenarioTree(rs, ps);
			ps.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return instance;
	}

	public T read(Class<T> clazz, long id) {
		return null;
	}

	public List<T> readAll(Class<T> clazz) {
		return null;
	}

}
