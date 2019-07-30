package implementation;

import java.nio.file.Files;
import java.nio.file.Paths;

import com.fizzed.rocker.BindableRockerModel;
import com.fizzed.rocker.Rocker;
import com.fizzed.rocker.RockerModel;
import com.fizzed.rocker.compiler.TemplateCompiler;
import com.fizzed.rocker.runtime.ArrayOfByteArraysOutput;
import com.fizzed.rocker.runtime.RockerRuntime;

public class RockerTest {

    public static void main(String[] args) {
        RockerRuntime runtime = RockerRuntime.getInstance();
        //runtime.setReloading(true);
        
        
        System.out.println(Files.exists(Paths.get("src/test/java/implementation/index.rocker.html")));
        
        //BindableRockerModel model = Rocker.template("src/test/java/implementation/index.rocker.html");
        
        RockerModel model = runtime.getBootstrap().model("src/test/java/implementation/index.rocker.html");
        model.render();
        //runtime.getBootstrap().template(RockerModel.class, model);
        ArrayOfByteArraysOutput output = model.render(ArrayOfByteArraysOutput.FACTORY);
        byte[] bs = output.toByteArray();
        
        System.out.println(new String(bs));
    }

}
