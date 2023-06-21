function selectCheckboxFieldIfTextFieldIsNotEmpty(textFieldId, checkboxFieldId) {
  let textField = document.getElementById(textFieldId);
  let checkboxField = document.getElementById(checkboxFieldId);
  if (!textField) {
    return;
  }

  textField.addEventListener("input", function () {
    if (textField.value !== "") {
      checkboxField.checked = true;
      checkboxField.parentElement.classList.add("is-selected");
    } else {
      checkboxField.checked = false;
      checkboxField.parentElement.classList.remove("is-selected");
    }
  });
}

class FollowUpInputsEnabledWhenSelected {
  static init() {
    // Set the initial value on page load
    FollowUpInputsEnabledWhenSelected.disableOrEnableAll();
    // Set up event hooks so changing the radio button value adjusts all inputs
    document.querySelectorAll(".follow-up-inputs-enabled-when-selected input[type='radio']").forEach(radioButton => {
      radioButton.addEventListener('change', FollowUpInputsEnabledWhenSelected.disableOrEnableAll);
    });
  }

  static disableOrEnableAll() {
    // Compute which follow-up should have its inputs enabled. Do this up-front since the follow-up can can be shared among different radio buttons.
    const checkedRadio = document.querySelector(".follow-up-inputs-enabled-when-selected input[type='radio']:checked");
    let enableFollowUpSelector = (checkedRadio && checkedRadio.dataset['followUp'] !== null && checkedRadio.dataset['followUp'].length > 0) ? checkedRadio.dataset['followUp'] : null;
    document.querySelectorAll(".follow-up-inputs-enabled-when-selected input[type='radio']").forEach(radioButton => {
      const radioButtonFollowUpSelector = radioButton.dataset['followUp'];
      const enable = (!!enableFollowUpSelector) && (radioButtonFollowUpSelector === enableFollowUpSelector);
      document.querySelector(radioButtonFollowUpSelector).querySelectorAll('input').forEach(followUpInput => {
        followUpInput.disabled = !enable;
      });
    });
  }
}

window.onload = function () {
  noneOfTheAbove.init();
  followUpQuestion.init();
  FollowUpInputsEnabledWhenSelected.init();
}
