$(document).ready(function(){

    $("#save_button1").click(function(){
        var email = $("#email").val();
        var password = $("#password").val();
        var qr_path = "/api/totp-code?email=" + email + "&password=" + password;

        $("#qr_img").prop("src", qr_path);
        $("#qr_div").show();

    });

    $("#login_button").click(function(){
        $.post(
            "/api/verify-login",
            $("#auth_code").val()=="" ?
                {
                    email: $("#email").val(),
                    password: $("#password").val()
                } :
                {
                    email: $("#email").val(),
                    password: $("#password").val(),
                    auth_code: $("#auth_code").val()
                },

            function(data, status){
                if("success" == data.type) {
                    $("#message").text("Login credentials are valid.");
                    $("#message").toggleClass("error_message", false);
                    $("#message").toggleClass("success_message", true);
                } else {
                    $("#message").text(data.message);
                    $("#message").toggleClass("error_message", true);
                    $("#message").toggleClass("success_message", false);
                }
            }
        );
    });


    $("#load_button").click(function(){
        $.get(
            "/api/totp-code",

            {
                email: $("#email").val(),
                password: $("#password").val()
            },

            function(data, status){
                if("success" == data.type) {
                    $("#qr_img").prop("src", data.data.img_src);
                    $("#qr_div").removeClass("hidden");
                    $("#message").text("QR code is loaded");
                    $("#message").toggleClass("error_message", false);
                    $("#message").toggleClass("success_message", true);
                } else {
                    $("#message").text(data.message);
                    $("#message").toggleClass("error_message", true);
                    $("#message").toggleClass("success_message", false);
                }
            }
        );
    });


    $("#enable_button").click(function(){
        $.post(
            "/api/enable-2fa",

            {
                email: $("#email").val(),
                password: $("#password").val(),
                auth_code: $("#auth_code").val()
            },

            function(data, status){
                if("success" == data.type) {
                    $("#message").text("2FA is enabled for user: " + data.data.email);
                    $("#message").toggleClass("error_message", false);
                    $("#message").toggleClass("success_message", true);
                } else {
                    $("#message").text(data.message);
                    $("#message").toggleClass("error_message", true);
                    $("#message").toggleClass("success_message", false);
                }
            }
        );
    });

});