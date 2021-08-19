package com.kazurayam.materialstoreexamples;

import com.kazurayam.subprocessj.Subprocess;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Executes "security" command in the bash shell
 * to make query to the KeyChain application of macOS.
 * kazurayam stores his credentials into the KeyChain app on his Mac.
 * This class will successfully work only on his MacBook Air.
 */
public class MyKeyChainAccessor {

    MyKeyChainAccessor() {}

    String findPassword(String server, String account)
            throws IOException, InterruptedException {
        Objects.requireNonNull(server);
        Objects.requireNonNull(account);

        List<String> command = Arrays.asList(
                "/usr/bin/security", "find-internet-password",
                "-s", server, "-a", account, "-w");
        //println "command:${command}";
        Subprocess.CompletedProcess cp = new Subprocess().run(command);
        assert cp.returncode() == 0;
        //println "stdout:${cp.stdout()}";
        //println "stderr:${cp.stderr()}";
        if (cp.stdout().size() > 0) {
            return cp.stdout().get(0);
        } else {
            return null;
        }
    }
}
