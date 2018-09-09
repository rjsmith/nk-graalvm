package uk.co.rsbatechnology.lang.graalvm;

import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.nkf.INKFRequestReadOnly;
import org.netkernel.layer0.nkf.NKFException;
import org.netkernel.layer0.representation.IReadableBinaryStreamRepresentation;
import org.netkernel.layer0.util.RequestScopeClassLoader;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;
import org.netkernel.urii.impl.NetKernelException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.graalvm.polyglot.*;
import org.graalvm.polyglot.io.ByteSequence;

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
                               
            Context polyglot = Context.newBuilder(graalVMLanguage).allowAllAccess(true).build();
            
            // Check language is available at runtime
            Set<String> languages = polyglot.getEngine().getLanguages().keySet();
            if (!languages.contains(graalVMLanguage)) {
                throw new NKFException("Language: " + graalVMLanguage + " is unknown to GraalVM");
            }
            
            // Add NK context object to guest language scope as a GraalVM Polyglot value.
            polyglot.getPolyglotBindings().putMember("context", context);

            ClassLoader oldCL=Thread.currentThread().getContextClassLoader();
            try {
                
                ClassLoader CL=new RequestScopeClassLoader(context.getKernelContext().getThisKernelRequest().getRequestScope());
                Thread.currentThread().setContextClassLoader(CL);
                
                switch (graalVMLanguage) {
                
                case "llvm":
                    IReadableBinaryStreamRepresentation bstream = context.source("arg:operator", IReadableBinaryStreamRepresentation.class);
                    InputStream stream = bstream.getInputStream();
                    byte[] targetArray = new byte[stream.available()];
                    stream.read(targetArray);

                    Source source = Source.newBuilder("llvm", ByteSequence.create(targetArray), "<literal>").buildLiteral();
                    polyglot.eval(source);
                    
                    break;
                default:
                    polyglot.eval(graalVMLanguage, context.source("arg:operator", String.class));    
                }
                
                
            } catch (PolyglotException e) {
                e.printStackTrace();
                NetKernelException nke=new NetKernelException("GraalVMException", e.getMessage(), e);
                throw nke;
            } finally {
                Thread.currentThread().setContextClassLoader(oldCL);                
            }
          
     
        }
    }
    
}
