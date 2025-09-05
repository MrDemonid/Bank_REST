package com.example.bankcards;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@OpenAPIDefinition(
        info = @Info(
                title = "Разработка Системы Управления Банковскими Картами",
                description = "Создание и управление картами, переводы между своими счетами, создание и управление пользователями",
                version = "1.0.0",
                contact = @Contact(
                        name = "Andrey Hlus",
                        email = "andnot@yandex.ru",
                        url = "https://github.com/MrDemonid"
                )
        )
)
@SpringBootApplication
public class BankRestApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankRestApplication.class, args);
    }

}
