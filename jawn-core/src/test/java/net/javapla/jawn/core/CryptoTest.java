package net.javapla.jawn.core;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

import net.javapla.jawn.core.Crypto.Encrypter;
import net.javapla.jawn.core.Crypto.Signer;

public class CryptoTest {

    @Test
    public void expandSecret() {
        String expanded = Crypto.SecretGenerator.expandSecret("test", 64);
        assertThat(expanded).hasLength(64);
        
        expanded = Crypto.SecretGenerator.expandSecret("test", 8);
        assertThat(expanded).hasLength(8);
        assertThat(expanded).isEqualTo("testtest");
        
        expanded = Crypto.SecretGenerator.expandSecret("testtest", 4);
        assertThat(expanded).hasLength(4);
        assertThat(expanded).isEqualTo("test");
    }

    @Test
    public void test() {
        Signer sha256 = Crypto.Signer.SHA256("secretkeyblabalbalbalblablablablablablab");
        System.out.println(sha256.outputLength());
        String sign = sha256.sign("kagemanden fra otterup");
        System.out.println(sign);
        System.out.println(sign.length());
        
        System.out.println();
        
        System.out.println(Crypto.Encrypter.AES("datter").keyLength());
        System.out.println(Crypto.Encrypter.AES("secretdatatat").encrypt("henninghenning") + "  " + Crypto.Encrypter.AES("secretdatatat").encrypt("henninghenning").length());
        System.out.println(Crypto.Encrypter.AES("secretsecretsecretsecretsecretsecretsecretsecretsecret").encrypt("kagekagekagekage"));
        
        Encrypter aes = Crypto.Encrypter.AES("ssshhhhhhhhhhh!!!!");
        String originalString = "howtodoinjava.com";
        String encrypt = aes.encrypt(originalString);
        String decrypt = aes.decrypt(encrypt);
        System.out.println(encrypt);
        System.out.println(decrypt);
    }

}
