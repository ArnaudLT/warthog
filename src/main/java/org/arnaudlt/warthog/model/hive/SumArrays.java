package org.arnaudlt.warthog.model.hive;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentTypeException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.parse.SemanticException;
import org.apache.hadoop.hive.ql.udf.generic.AbstractGenericUDAFResolver;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDAFEvaluator;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDAFParameterInfo;
import org.apache.hadoop.hive.serde2.objectinspector.*;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.AbstractPrimitiveJavaObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorUtils;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Description(
        name = "Sum arrays of numbers",
        value = "_FUNC_(x) - Returns the vector sum",
        extended = "The function takes as argument any array of numeric types and returns an array."
)
public class SumArrays extends AbstractGenericUDAFResolver {


    @Override
    public GenericUDAFEvaluator getEvaluator(GenericUDAFParameterInfo info) throws SemanticException {

        int argumentsCount = info.getParameterObjectInspectors().length;
        if (argumentsCount != 1) {
            throw new UDFArgumentLengthException("Please specify only one argument.");
        }

        ObjectInspector argument = info.getParameterObjectInspectors()[0];
        if (argument.getCategory() != ObjectInspector.Category.LIST) {
            throw new UDFArgumentTypeException(0, "Please provide a list of numbers.");
        }

        ObjectInspector objectInspector = ((StandardListObjectInspector) argument).getListElementObjectInspector();

        return switch (((AbstractPrimitiveJavaObjectInspector) objectInspector).getPrimitiveCategory()) {
            case BYTE, SHORT, INT, LONG -> new GenericUDAFSumLongArray();
            case FLOAT, DOUBLE, DECIMAL -> new GenericUDAFSumDoubleArray();
            default ->
                    throw new UDFArgumentTypeException(0, "Expected arrays of numbers, but arrays of " + objectInspector.getTypeName() + " detected");
        };

    }


    public static class GenericUDAFSumLongArray extends GenericUDAFEvaluator {

        private PrimitiveObjectInspector elementOI;
        private ObjectInspectorConverters.Converter inputConverter;

        @Override
        public ObjectInspector init(Mode m, ObjectInspector[] parameters)
                throws HiveException {
            super.init(m, parameters);

            elementOI = PrimitiveObjectInspectorFactory.javaLongObjectInspector;

            inputConverter = ObjectInspectorConverters.getConverter(
                    parameters[0],
                    ObjectInspectorFactory.getStandardListObjectInspector(
                            PrimitiveObjectInspectorFactory.javaLongObjectInspector
                    )
            );

            return ObjectInspectorFactory.getStandardListObjectInspector(
                    PrimitiveObjectInspectorFactory.writableLongObjectInspector
            );
        }


        static class ArrayAggregationBuffer implements AggregationBuffer {

            List<LongWritable> container;
        }

        @Override
        public void reset(AggregationBuffer agg) {

            ((ArrayAggregationBuffer) agg).container = new ArrayList<>();
        }

        @Override
        public AggregationBuffer getNewAggregationBuffer() {

            ArrayAggregationBuffer ret = new ArrayAggregationBuffer();
            reset(ret);
            return ret;
        }

        @Override
        public void iterate(AggregationBuffer agg, Object[] parameters) {

            merge(agg, parameters[0]);
        }

        @Override
        public Object terminatePartial(AggregationBuffer agg) {

            return terminate(agg);
        }

        @Override
        public void merge(AggregationBuffer agg, Object partial) {

            List<Object> p = (List<Object>) inputConverter.convert(partial);
            if (p != null) {
                ArrayAggregationBuffer myagg = (ArrayAggregationBuffer) agg;
                sumArray(p, myagg);
            }
        }

        @Override
        public Object terminate(AggregationBuffer agg) {

            ArrayAggregationBuffer accumulator = (ArrayAggregationBuffer) agg;
            return new ArrayList<>(accumulator.container);
        }

        private void sumArray(List<Object> p, ArrayAggregationBuffer myagg) {

            int currentSize = myagg.container.size();

            for (int i = 0; i < p.size(); i++) {
                Object elements = p.get(i);
                if (elements != null) {
                    long v = PrimitiveObjectInspectorUtils.getLong(p.get(i), elementOI);
                    if (i >= currentSize) {
                        myagg.container.add(new LongWritable(v));
                    } else {
                        LongWritable current = myagg.container.get(i);
                        current.set(current.get() + v);
                    }
                } else {
                    if (i >= currentSize) {
                        myagg.container.add(new LongWritable(0));
                    }
                }
            }
        }
    }


    public static class GenericUDAFSumDoubleArray extends GenericUDAFEvaluator {

        private PrimitiveObjectInspector elementOI;

        private ObjectInspectorConverters.Converter inputConverter;


        @Override
        public ObjectInspector init(Mode m, ObjectInspector[] parameters)
                throws HiveException {
            super.init(m, parameters);

            elementOI = PrimitiveObjectInspectorFactory.javaDoubleObjectInspector;

            inputConverter = ObjectInspectorConverters.getConverter(
                    parameters[0],
                    ObjectInspectorFactory.getStandardListObjectInspector(
                            PrimitiveObjectInspectorFactory.javaDoubleObjectInspector
                    )
            );

            return ObjectInspectorFactory.getStandardListObjectInspector(
                    PrimitiveObjectInspectorFactory.writableDoubleObjectInspector
            );
        }


        static class ArrayAggregationBuffer implements AggregationBuffer {
            List<DoubleWritable> container;
        }


        @Override
        public void reset(AggregationBuffer agg) {
            ((ArrayAggregationBuffer) agg).container = new ArrayList<>();
        }


        @Override
        public AggregationBuffer getNewAggregationBuffer() {

            ArrayAggregationBuffer ret = new ArrayAggregationBuffer();
            reset(ret);
            return ret;
        }


        @Override
        public void iterate(AggregationBuffer agg, Object[] parameters) {

            merge(agg, parameters[0]);
        }


        @Override
        public Object terminatePartial(AggregationBuffer agg) {

            return terminate(agg);
        }


        @Override
        public void merge(AggregationBuffer agg, Object partial) {

            List<Object> p = (List<Object>) inputConverter.convert(partial);
            if (p != null) {
                ArrayAggregationBuffer accumulator = (ArrayAggregationBuffer) agg;
                sumArray(p, accumulator);
            }
        }


        @Override
        public Object terminate(AggregationBuffer agg) {

            ArrayAggregationBuffer accumulator = (ArrayAggregationBuffer) agg;
            return new ArrayList<>(accumulator.container);
        }


        private void sumArray(List<Object> p, ArrayAggregationBuffer myagg) {

            int currentSize = myagg.container.size();

            for (int i = 0; i < p.size(); i++) {
                Object elements = p.get(i);
                if (elements != null) {
                    double v = PrimitiveObjectInspectorUtils.getDouble(p.get(i), elementOI);
                    if (i >= currentSize) {
                        myagg.container.add(new DoubleWritable(v));
                    } else {
                        DoubleWritable current = myagg.container.get(i);
                        current.set(current.get() + v);
                    }
                } else {
                    if (i >= currentSize) {
                        myagg.container.add(new DoubleWritable(0.0));
                    }
                }
            }
        }
    }
}
