package br.ufrn.ppgsc.scenario.analyzer.common.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

public abstract class MemberUtil {

	public static String getStandartMethodSignature(Member member) {
		StringBuilder result = new StringBuilder();
		Class<?>[] ptypes = null;

		if (member instanceof Method) {
			result.append(member.getDeclaringClass().getName());
			result.append(".");
			result.append(member.getName());

			ptypes = ((Method) member).getParameterTypes();
		} else if (member instanceof Constructor) {
			result.append(member.getName());
			ptypes = ((Constructor<?>) member).getParameterTypes();
		}

		result.append("(");

		for (int i = 0; i < ptypes.length; i++) {
			result.append(ptypes[i].getCanonicalName());

			if (i + 1 < ptypes.length)
				result.append(",");
		}

		result.append(")");

		return result.toString();
	}

}
