/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import model.BankDTO;
import utils.HttpClientUtil;

import java.lang.reflect.Type;
import java.util.List;
/**
 *
 * @author D E L L
 */
public class BankSevice {
    private static final String BANK_API_URL = "https://api.vietqr.io/v2/banks";

    public List<BankDTO> getAllBanks() throws Exception {
        // Gọi API
        String response = HttpClientUtil.get(BANK_API_URL);

        // Parse JSON
        JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();

        // Lấy mảng "data"
        Type listType = new TypeToken<List<BankDTO>>(){}.getType();
        List<BankDTO> banks = new Gson().fromJson(jsonObject.get("data"), listType);

        return banks;
    }
}
