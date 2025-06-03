package com.zabotini.hamburgueriaz;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.textfield.TextInputEditText;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    final int PRICE_HAMB = 20;
    final int PRICE_CHEESE = 2;
    final int PRICE_BACON = 2;
    final int PRICE_ONION = 3;
    int basePrice;

    private static class Quantity {
        private Integer quantity; // private = restricted access

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer newQuantity) {
            this.quantity = newQuantity;
        }
    }

    private static class Price {
        private Integer price;

        public Integer getPrice() {
            return price;
        }

        public void setPrice(Integer newPrice) {
            this.price = newPrice;
        }
    }

    Price total = new Price();
    Quantity count = new Quantity();

    private TextInputEditText customerName;
    private TextView numQuantity;
    private TextView numTotal;
    private SwitchCompat swBacon;
    private SwitchCompat swExtraCheese;
    private SwitchCompat swOnion;
    private Button btnSendOrder;
    private Button btnMinus;
    private Button btnPlus;
    boolean bacon = false;
    boolean cheese = false;
    boolean onionRings = false;
    StringBuilder concatOrder = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //início de coisas do sistema, não alterar abaixo
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //fim de coisas do sistema, não alterar acima
// --------------------------------------------------------------------------------------------------------//
        // inicializando valores de quantidade e preço
        count.setQuantity(1);
        total.setPrice(PRICE_HAMB);
        basePrice = PRICE_HAMB;

        // instanciando componentes da tela
        customerName = findViewById(R.id.text_customer_name);
        numQuantity = findViewById(R.id.num_quantity);
        numTotal = findViewById(R.id.num_total);
        btnMinus = findViewById(R.id.btn_minus);
        btnPlus = findViewById(R.id.btn_plus);
        swBacon = findViewById(R.id.switch_add_bacon);
        swExtraCheese = findViewById(R.id.switch_add_cheese);
        swOnion = findViewById(R.id.switch_add_onion);
        btnSendOrder = findViewById(R.id.btn_send_order);
        String recipientEmail = Objects.requireNonNull(customerName.getText()).toString().trim().toLowerCase() + "@gmail.com";
        final String[] subject = new String[1];

        checkExtras();

        // incrementa quantidade de lanches
        btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count.setQuantity(count.quantity + 1);
                updateOrder();
            }
        });

        // decrementa quantidade de lanches
        btnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (count.quantity > 1) {
                    count.setQuantity(count.quantity - 1);
                    updateOrder();
                }
            }
        });

        btnSendOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subject[0] = "Pedido #0001 de " + customerName.getText().toString();
                recipientEmail.trim();
                buildOrderMessage(concatOrder);
                sendOrder(recipientEmail, subject[0], concatOrder.toString());
            }
        });

    } // fim do onCreate/override

    // --------------------------------------------------------------------------------------------------------//
    // atualiza os valores na tela
    void updateOrder() {
        numQuantity.setText(MessageFormat.format("{0}", count.quantity));
        total.setPrice(count.quantity * basePrice);
        numTotal.setText(MessageFormat.format("{0},00", total.getPrice().toString()));
        getCustomerName(String.valueOf(customerName));
    }

    // abstração do listener para os switches + preço dos extras
    private void listenSwitch(SwitchCompat sw, boolean bl, int ext) {
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean bl) {
                if (bl) {
//                    bl = true;
                    basePrice += ext;
                } else {
//                    bl = false;
                    basePrice -= ext;
                }
                updateOrder();
            }
        });
    }

    // instanciamento dos listeners dos switches
    private void checkExtras() {
        listenSwitch(swBacon, bacon, PRICE_BACON);
        listenSwitch(swExtraCheese, cheese, PRICE_CHEESE);
        listenSwitch(swOnion, onionRings, PRICE_ONION);
    }

    private void sendOrder(String recipientEmail, String subject, String body) {
        openEmail(recipientEmail, subject, concatOrder.toString());
    }

    // pega o nome do cliente do input
    private void getCustomerName(String name) {
        name = Objects.requireNonNull(customerName.getText()).toString();
    }

    // chama o app de email padrão do celular
    private void openEmail(String recipientEmail, String subject, String body) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        String mailTo = "mailto:" + recipientEmail +
                "?subject=" + Uri.encode(subject) +
                "&body=" + Uri.encode(body);
        emailIntent.setData(Uri.parse(mailTo));
        startActivity(Intent.createChooser(emailIntent, "Enviar e-mail"));
    }
    //cria o corpo do email
    private void buildOrderMessage(StringBuilder message) {

        LocalDateTime orderTimeStamp = LocalDateTime.now();
        DateTimeFormatter orderTimeStampFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy" + " às " + "HH:mm:ss");

        message.append("Olá,  ").append(Objects.requireNonNull(customerName.getText()).toString()).append("\n\n");
        message.append("Segue o resumo do seu pedido feito em").append("\n");
        message.append(orderTimeStamp).append("\n\n");
        message.append("Quantidade de lanches: ").append(count.quantity).append("\n");
        message.append("Adicionais: ").append("\n");
        if (bacon) {
            message.append("Bacon: sim").append("\n");
        } else {
            message.append("Bacon: não").append("\n");
        }
        if (cheese) {
            message.append("Queijo: sim").append("\n");
        } else {
            message.append("Queijo: não").append("\n");
        }
        if (onionRings) {
            message.append("Onion Rings: sim").append("\n\n");
        } else {
            message.append("Onion Rings: não").append("\n\n");
        }
        message.append("Total: R$").append(total.getPrice()).append(",00\n\n");
        message.append("Obrigado por comprar conosco!");
    }
}
