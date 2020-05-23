package br.ufrn.ppgsc.scenario.analyzer.cdynamic.aspects;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Map;
import java.util.Stack;

import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.ConstructorSignature;
import org.aspectj.lang.reflect.MethodSignature;

import br.ufrn.ppgsc.scenario.analyzer.cdynamic.db.DatabaseService;
import br.ufrn.ppgsc.scenario.analyzer.cdynamic.model.RuntimeNode;
import br.ufrn.ppgsc.scenario.analyzer.cdynamic.model.RuntimeQuery;
import br.ufrn.ppgsc.scenario.analyzer.cdynamic.model.RuntimeScenario;
import br.ufrn.ppgsc.scenario.analyzer.cdynamic.model.SystemExecution;
import br.ufrn.ppgsc.scenario.analyzer.cdynamic.util.RuntimeCallGraph;
import br.ufrn.ppgsc.scenario.analyzer.common.annotations.arq.Scenario;
import br.ufrn.ppgsc.scenario.analyzer.common.util.MemberUtil;

public abstract class AspectUtil {

	// Cada thread tem uma pilha de execução diferente, tanto para cenários quanto para nós
	private final static Map<Long, Stack<RuntimeScenario>> thread_scenarios_map = new Hashtable<Long, Stack<RuntimeScenario>>();

	private final static Map<Long, Stack<RuntimeNode>> thread_nodes_map = new Hashtable<Long, Stack<RuntimeNode>>();

	protected static boolean isScenarioEntryPoint(Member member, Class<? extends Annotation> ann_cls, boolean isNodeStackEmpty) {
		if (ann_cls == null)
			return isNodeStackEmpty;

		Annotation annotation = null;

		if (member instanceof Method)
			annotation = ((Method) member).getAnnotation(ann_cls);

		return annotation != null;
	}

	protected static String getEntryPointName(Member member, Class<? extends Annotation> ann_cls) {
		if (ann_cls == Scenario.class)
			return ((Method) member).getAnnotation(Scenario.class).name();
		else
			return "Entry point for "
					+ member.getDeclaringClass().getSimpleName() + "."
					+ member.getName();
	}

	protected static Member getMember(Signature sig) {
		if (sig instanceof MethodSignature)
			return ((MethodSignature) sig).getMethod();
		else if (sig instanceof ConstructorSignature)
			return ((ConstructorSignature) sig).getConstructor();

		return null;
	}

	protected static Stack<RuntimeNode> getOrCreateRuntimeNodeStack() {
		long thread_id = Thread.currentThread().getId();
		Stack<RuntimeNode> nodes_stack = thread_nodes_map.get(thread_id);

		if (nodes_stack == null) {
			nodes_stack = new Stack<RuntimeNode>();
			thread_nodes_map.put(thread_id, nodes_stack);
		}

		return nodes_stack;
	}

	protected static Stack<RuntimeScenario> getOrCreateRuntimeScenarioStack() {
		long thread_id = Thread.currentThread().getId();
		Stack<RuntimeScenario> scenarios_stack = thread_scenarios_map.get(thread_id);

		if (scenarios_stack == null) {
			scenarios_stack = new Stack<RuntimeScenario>();
			thread_scenarios_map.put(thread_id, scenarios_stack);
		}

		return scenarios_stack;
	}

	protected static void popStacksAndPersistData(long time, Member member, Class<? extends Annotation> annotation) {
		SystemExecution execution = RuntimeCallGraph.getInstance().getCurrentExecution();

		Stack<RuntimeScenario> scenarios_stack = AspectUtil.getOrCreateRuntimeScenarioStack();
		Stack<RuntimeNode> nodes_stack = AspectUtil.getOrCreateRuntimeNodeStack();

		try {
			// Desempilha o último método
			RuntimeNode node = nodes_stack.pop();

			// Configura o tempo de executação total
			node.setExecutionTime(time);

			if (time == 0) {
				// O node raiz não tem pai
				if (node.getParent() != null)
					node.getParent().removeChildren(node);

				node.setParent(null);
			} else if (time == -1) {
				node.setRealExecutionTime(time);
			} else {
				// Configura o tempo de executação real (subtraindo o tempo dos
				// filhos)
				long real_time = time;
				for (RuntimeNode child : node.getChildren())
					real_time -= child.getExecutionTime();

				node.setRealExecutionTime(real_time);
			}

			/*
			 * Caso o método seja um método de entrada de um cenário, significa
			 * que o cenário terminou de executar.
			 */
			if (AspectUtil.isScenarioEntryPoint(member, annotation, nodes_stack.empty()))
				scenarios_stack.pop();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Se a pilha de cenários estiver vazia, salva as informações no banco de dados
		if (scenarios_stack.empty())
			DatabaseService.saveResults(execution);
	}

	protected static void setException(Throwable t, Member m) {
		Stack<RuntimeNode> nodes_stack = getOrCreateRuntimeNodeStack();

		// Se estiver vazia é porque o método não faz parte de cenário
		if (!nodes_stack.empty()) {
			RuntimeNode node = nodes_stack.peek();

			// Testa se foi o método que capturou ou lançou a exceção
			if (node.getMemberSignature().equals(MemberUtil.getStandartMethodSignature(m)))
				node.setExceptionMessage(t.toString());
		}
	}

	protected static void addQueryToNode(String query, String type) {
		Stack<RuntimeNode> stack_nodes = AspectUtil.getOrCreateRuntimeNodeStack();

		if (stack_nodes.isEmpty()) {
			System.out.println("Captured SQL is out of the scenarios of interest");
		} else {
			RuntimeNode node = stack_nodes.peek();
			node.addQuery(new RuntimeQuery(query, type));
		}
	}

}
