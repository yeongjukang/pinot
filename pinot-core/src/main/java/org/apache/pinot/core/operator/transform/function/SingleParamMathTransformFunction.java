/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pinot.core.operator.transform.function;

import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Map;
import org.apache.pinot.core.operator.blocks.ProjectionBlock;
import org.apache.pinot.core.operator.transform.TransformResultMetadata;
import org.apache.pinot.segment.spi.datasource.DataSource;


/**
 * A group of commonly used math transformation which has only one single parameter,
 * including abs, exp, ceil, floor, sqrt.
 */
public abstract class SingleParamMathTransformFunction extends BaseTransformFunction {
  private TransformFunction _transformFunction;

  @Override
  public void init(List<TransformFunction> arguments, Map<String, DataSource> dataSourceMap) {
    Preconditions.checkArgument(arguments.size() == 1, "Exactly 1 argument is required for transform function: %s",
        getName());
    TransformFunction transformFunction = arguments.get(0);
    Preconditions.checkArgument(!(transformFunction instanceof LiteralTransformFunction),
        "Argument cannot be literal for transform function: %s", getName());
    Preconditions.checkArgument(transformFunction.getResultMetadata().isSingleValue(),
        "Argument must be single-valued for transform function: %s", getName());

    _transformFunction = transformFunction;
  }

  @Override
  public TransformResultMetadata getResultMetadata() {
    return DOUBLE_SV_NO_DICTIONARY_METADATA;
  }

  @Override
  public double[] transformToDoubleValuesSV(ProjectionBlock projectionBlock) {
    int length = projectionBlock.getNumDocs();

    if (_doubleValuesSV == null || _doubleValuesSV.length < length) {
      _doubleValuesSV = new double[length];
    }

    double[] values = _transformFunction.transformToDoubleValuesSV(projectionBlock);
    applyMathOperator(values, projectionBlock.getNumDocs());
    return _doubleValuesSV;
  }

  abstract protected void applyMathOperator(double[] values, int length);

  public static class AbsTransformFunction extends SingleParamMathTransformFunction {
    public static final String FUNCTION_NAME = "abs";

    @Override
    public String getName() {
      return FUNCTION_NAME;
    }

    @Override
    protected void applyMathOperator(double[] values, int length) {
      for (int i = 0; i < length; i++) {
        _doubleValuesSV[i] = Math.abs(values[i]);
      }
    }
  }

  public static class CeilTransformFunction extends SingleParamMathTransformFunction {
    public static final String FUNCTION_NAME = "ceil";

    @Override
    public String getName() {
      return FUNCTION_NAME;
    }

    @Override
    protected void applyMathOperator(double[] values, int length) {
      for (int i = 0; i < length; i++) {
        _doubleValuesSV[i] = Math.ceil(values[i]);
      }
    }
  }

  public static class ExpTransformFunction extends SingleParamMathTransformFunction {
    public static final String FUNCTION_NAME = "exp";

    @Override
    public String getName() {
      return FUNCTION_NAME;
    }

    @Override
    protected void applyMathOperator(double[] values, int length) {
      for (int i = 0; i < length; i++) {
        _doubleValuesSV[i] = Math.exp(values[i]);
      }
    }
  }

  public static class FloorTransformFunction extends SingleParamMathTransformFunction {
    public static final String FUNCTION_NAME = "floor";

    @Override
    public String getName() {
      return FUNCTION_NAME;
    }

    @Override
    protected void applyMathOperator(double[] values, int length) {
      for (int i = 0; i < length; i++) {
        _doubleValuesSV[i] = Math.floor(values[i]);
      }
    }
  }

  public static class LnTransformFunction extends SingleParamMathTransformFunction {
    public static final String FUNCTION_NAME = "ln";

    @Override
    public String getName() {
      return FUNCTION_NAME;
    }

    @Override
    protected void applyMathOperator(double[] values, int length) {
      for (int i = 0; i < length; i++) {
        _doubleValuesSV[i] = Math.log(values[i]);
      }
    }
  }

  public static class Log2TransformFunction extends SingleParamMathTransformFunction {
    public static final String FUNCTION_NAME = "log2";
    public static final double LOG_BASE = Math.log(2.0);

    @Override
    public String getName() {
      return FUNCTION_NAME;
    }

    @Override
    protected void applyMathOperator(double[] values, int length) {
      for (int i = 0; i < length; i++) {
        _doubleValuesSV[i] = Math.log(values[i]) / LOG_BASE;
      }
    }
  }

  public static class Log10TransformFunction extends SingleParamMathTransformFunction {
    public static final String FUNCTION_NAME = "log10";

    @Override
    public String getName() {
      return FUNCTION_NAME;
    }

    @Override
    protected void applyMathOperator(double[] values, int length) {
      for (int i = 0; i < length; i++) {
        _doubleValuesSV[i] = Math.log10(values[i]);
      }
    }
  }

  public static class SqrtTransformFunction extends SingleParamMathTransformFunction {
    public static final String FUNCTION_NAME = "sqrt";

    @Override
    public String getName() {
      return FUNCTION_NAME;
    }

    @Override
    protected void applyMathOperator(double[] values, int length) {
      for (int i = 0; i < length; i++) {
        _doubleValuesSV[i] = Math.sqrt(values[i]);
      }
    }
  }

  public static class SignTransformFunction extends SingleParamMathTransformFunction {
    public static final String FUNCTION_NAME = "sign";

    @Override
    public String getName() {
      return FUNCTION_NAME;
    }

    @Override
    protected void applyMathOperator(double[] values, int length) {
      for (int i = 0; i < length; i++) {
        _doubleValuesSV[i] = Math.signum(values[i]);
      }
    }
  }
}
