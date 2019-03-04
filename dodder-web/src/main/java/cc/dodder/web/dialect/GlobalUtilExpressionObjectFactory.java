package cc.dodder.web.dialect;

import cc.dodder.common.util.StringUtil;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.expression.IExpressionObjectFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class GlobalUtilExpressionObjectFactory implements IExpressionObjectFactory {

	private static final String GLOBAL_UTIL__EVALUATION_VARIABLE_NAME = "dodderUtil";

	private static final Set<String> ALL_EXPRESSION_OBJECT_NAMES = Collections.unmodifiableSet(
			new HashSet<>(Arrays.asList(GLOBAL_UTIL__EVALUATION_VARIABLE_NAME)));


	@Override
	public Set<String> getAllExpressionObjectNames() {
		return ALL_EXPRESSION_OBJECT_NAMES;
	}

	@Override
	public Object buildObject(IExpressionContext context, String expressionObjectName) {
		if (expressionObjectName.equals(GLOBAL_UTIL__EVALUATION_VARIABLE_NAME)) {
			return new StringUtil();
		}
		return null;
	}

	@Override
	public boolean isCacheable(String expressionObjectName) {
		return GLOBAL_UTIL__EVALUATION_VARIABLE_NAME != null && GLOBAL_UTIL__EVALUATION_VARIABLE_NAME.equals(expressionObjectName);
	}
}
