@file:Repository("https://litarvan.github.io/maven")
@file:DependsOn("fr.litarvan:openauth:1.1.3")

import gbx.proxy.utils.*
import gbx.proxy.ProxyBootstrap.*

import com.mojang.authlib.*
import com.mojang.authlib.yggdrasil.*

import java.net.*
import java.util.*
import java.awt.*
import java.io.*

import javax.swing.*
import javax.swing.text.*

import fr.litarvan.openauth.microsoft.*

object MojangSessionService : MinecraftClientSessionService {
    val BASE_SERVICE = YggdrasilAuthenticationService(Proxy.NO_PROXY).createMinecraftSessionService();

    override fun joinServer(profile: GameProfile, token: String, serverId: String) {
        BASE_SERVICE.joinServer(profile, token, serverId);
    }
}

@Entrypoint
fun entry() {
    val authenticator = MicrosoftAuthenticator();
    SESSION_SERVICE = MojangSessionService

    println("[MS Auth] Started!")
    println("[MS Auth] Set session service to: " + SESSION_SERVICE)

    Gui().open("msft auth", { name, password ->
        println("[MS Auth] Logging in with email '$name'...");
        try {
            val result = authenticator.loginWithCredentials(name, password);

            NAME = result.profile.name;
            UUID = result.profile.id;
            ACCESS_TOKEN = result.accessToken;

            JOptionPane.showMessageDialog(
                null,
                "successfully logged in\n($NAME)",
                "success",
                JOptionPane.INFORMATION_MESSAGE
            )

            println("[MS Auth] Successfully logged in! ($name -> $NAME)")
        } catch (e: Exception) {
            JOptionPane.showMessageDialog(
                null,
                e.message!!.toLowerCase(),
                "error during login",
                JOptionPane.ERROR_MESSAGE
            )

            println("[MS Auth] Failed to log in! ($name)")
        }
    })
}

@Destructor
fun destruct() {
    println("[MS Auth] Stopped!")
}

class Gui: JFrame() {
    private var usernameField: JTextField
    private var passwordField: JPasswordField
    private var loginButton: JButton

    fun open(name: String, login: (String, String) -> Unit) {
        loginButton.addActionListener {
            login(usernameField.text, String(passwordField.password))
        }

        setSize(295, 240)
        title = name
        isResizable = false
        isVisible = true
    }

    init {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        JFrame.setDefaultLookAndFeelDecorated(true)

        contentPane.layout = null

        JLabel("username").apply {
            setBounds(20, 20, 100, 20)
            contentPane.add(this)
        }
        usernameField = JTextField().apply {
            setBounds(20, 40, 240, 20)
            contentPane.add(this)
        }

        JLabel("password").apply {
            setBounds(20, 70, 100, 20)
            contentPane.add(this)
        }
        passwordField = JPasswordField().apply {
            setBounds(20, 90, 240, 20)
            contentPane.add(this)
        }

        loginButton = JButton("login").apply {
            setBounds(20, 130, 240, 20)
            contentPane.add(this)
        }
    }
}
