function toggleShowPassword(id, toggleId) {
    const togglePassword = document.getElementById(toggleId);
    const password = document.getElementById(id);
    // toggle the type attribute
    const type = password.getAttribute('type') === 'password' ? 'text' : 'password';
    password.setAttribute('type', type);
    // toggle the eye slash icon
    togglePassword.classList.toggle('fa-eye-slash');
}
