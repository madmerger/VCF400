// Function-key bindings: physical F3/F5/F8/F12 and ENTER behave like the 5250 keys (buttons carry data-fkey).
(function () {
  function fire(key) {
    var btn = document.querySelector('[data-fkey="' + key + '"]');
    if (btn && !btn.disabled) { btn.click(); return true; }
    return false;
  }
  document.addEventListener('keydown', function (ev) {
    var map = { F3: 'F3', F5: 'F5', F8: 'F8', F12: 'F12' };
    if (map[ev.key]) {
      if (fire(map[ev.key])) ev.preventDefault();
      return;
    }
    if (ev.key === 'Enter' && !(ev.target instanceof HTMLTextAreaElement) && !(ev.target instanceof HTMLButtonElement)) {
      // ENTER = default action (Submit / Continue / menu selection)
      var enterBtn = document.querySelector('[data-fkey="ENTER"]');
      if (enterBtn) { ev.preventDefault(); enterBtn.click(); }
    }
  });
  // Menu items: clicking a numbered option fills the option field and submits (number concept kept)
  document.querySelectorAll('[data-option]').forEach(function (el) {
    el.addEventListener('click', function () {
      var form = el.closest('form');
      var input = form && form.querySelector('input[name="option"]');
      if (input) { input.value = el.getAttribute('data-option'); form.requestSubmit(); }
    });
  });
  var first = document.querySelector('[autofocus]');
  if (first) first.focus();
})();
