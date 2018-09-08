package uk.co.rsbatechnology.lang.graalvm;

import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.nkf.INKFRequestReadOnly;
import org.netkernel.layer0.nkf.NKFException;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;

import java.util.Set;

import org.graalvm.polyglot.*;

public class GraalVMRuntime extends StandardAccessorImpl  {
    
    public GraalVMRuntime() {
        this.declareThreadSafe();
    }

    
    @Override
    public void onRequest(INKFRequestContext context) throws Exception
    {   if (context.getThisRequest().getVerb()==INKFRequestReadOnly.VERB_META)
        {   super.onMeta(context);
        }
        else
        {   
            //String sourceIdentifier=context.getThisRequest().getArgumentValue("operator");
            String graalVMLanguage=context.getThisRequest().getArgumentValue("graalvm-language");
                        
            String source = context.source("arg:operator", String.class);
        
            Context polyglot = Context.newBuilder(graalVMLanguage).allowAllAccess(true).build();
            
            // Check language is available at runtime
            Set<String> languages = polyglot.getEngine().getLanguages().keySet();
            if (!languages.contains(graalVMLanguage)) {
                throw new NKFException("Language: " + graalVMLanguage + " is unknown to GraalVM");
            }
            
            // Add NK context object to guest language scope as a GraalVM Polyglot value.
            polyglot.getPolyglotBindings().putMember("nkContext", context);

            try {
                Value str = polyglot.eval(graalVMLanguage, source);
                String result = str.asString();
                
                context.createResponseFrom(result);
                
            } catch (PolyglotException pe) {
                pe.printStackTrace();
            }
        
        
/*            ((NKFContextImpl)context).setRuntimeSourceIdentifier(sourceIdentifier);
            ClassLoader oldCL=Thread.currentThread().getContextClassLoader();
            try
            {   ClassLoader CL=new RequestScopeClassLoader(context.getKernelContext().getThisKernelRequest().getRequestScope());
                Thread.currentThread().setContextClassLoader(CL);
                CompiledScript s=context.source("arg:operator", CompiledScript.class);
                Bindings b=new SimpleBindings();
                b.put("context", context);
                s.eval(b);
            }
            catch(ScriptException e)
            {   
                NetKernelException nke=new NetKernelException("JavascriptException", "line: "+e.getLineNumber()+"  "+e.getMessage(), e);
                throw nke;
            }
            finally
            {   Thread.currentThread().setContextClassLoader(oldCL);                
            }*/
        }
    }
    
}
