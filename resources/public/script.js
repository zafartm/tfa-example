$(document).ready(function(){

    function showError(text, object) {
        if(object) {
            var html = '<p>' + text + '</p>';
            for (k in object) {
                html = html + '<li>' + object[k] + ": " + k + '</li>';
            }
            $("#message").html(html);
        } else {
            $("#message").text(text);
        }
        $("#message").toggleClass("error_message", true);
        $("#message").toggleClass("success_message", false);
    };

    function showSuccess(text) {
        $("#message").text(text);
        $("#message").toggleClass("error_message", false);
        $("#message").toggleClass("success_message", true);
    };

    $(document).ajaxError(function(event,xhr,options,exc){
//      alert("An error occurred!");
//      console.log(event);
//      console.log(xhr);
//      console.log(options);
//      console.log(exc);
      console.log(xhr.responseJSON);
      showError(xhr.statusText, xhr.responseJSON.errors)
    });

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
                    showSuccess("Login credentials are valid.");
                } else {
                    showError(data.message);
                }
            }
        );
    });


    $("#load_button").click(function(){
        $("#qr_img").prop("src", "");
        $("#qr_div").toggleClass("hidden", true);
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
                    showSuccess("QR code is loaded");
                } else {
                    showError(data.message);
                }
            }
        );
    });


    $("#enable_button").click(function(){
        $.post(
            "/api/enable-2fa",

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
//                console.log(status);
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