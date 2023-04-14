jQuery.fn.extend({
  /*
   Options:
     * valueField: The selector of the field that will submit the value.
     * data: The array of dropdown options
     * clearInput: The selector of the "x" icon that clears the combobox.
     * customOption: (Optional) A function that allows submitting a value that's not in the data.
   */
  combobox: function (options) {
    const $input = $(this);
    const $valueField = $(options.valueField);
    const $clearInput = $(options.clearInput);
    let lastSelectedValue;

    const autocomplete = $input.autocomplete({
      minLength: 0,
      delay: 100,
      source: options.data,
      focus: function (event, ui) {
        const originalEventType = event.originalEvent.originalEvent.type; // don't select the item on a "mouseover" event, only "keydown"

        if (ui.item.value !== 'custom' && originalEventType === 'keydown') {
          const displayText = ui.item.displayName || ui.item.label;
          $input.val(displayText);
          $valueField.val(ui.item.value);
          lastSelectedValue = displayText;
        }

        return false;
      },
      change: function (event, ui) {
        if ($input.val() !== lastSelectedValue) {
          $valueField.val('');
        }
      },
      select: function (event, ui) {
        if (ui.item.value === 'custom') {
          $valueField.val('');
        } else {
          $input.val(ui.item.displayName);
          $valueField.val(ui.item.value);
          lastSelectedValue = ui.item.displayName;
        }

        return false;
      },
      response: function (event, ui) {
        let searchText = $input.val();
        if (typeof options.customOption === 'function' && searchText) {
          let customOption = {
            label: searchText,
            value: 'custom',
            displayName: options.customOption(searchText),
          };
          ui.content.unshift(customOption);
        }
        return ui.content;
      }
    })
    autocomplete.autocomplete("instance")._renderItem = function (ul, item) {
      const searchText = $input.val();

      return $("<li>")
        .attr('aria-label', item.displayName)
        .append("<div>" + highlightResult(searchText, item.displayName, item.label) + "</div>")
        .appendTo(ul);
    };

    const highlightResult = function(searchText, displayText, label) {
      const escapedSearchText = searchText.replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&');
      const searchRegex = new RegExp(escapedSearchText, "i");

      if (displayText.search(searchRegex) !== -1) {
        return displayText.replace(searchRegex, "<b>$&</b>");
      } else if (label.search(searchRegex) !== -1) {
        return label.replace(searchRegex, "<b>$&</b>");
      } else {
        return displayText;
      }
    }

    $clearInput.toggle(!!$input.val());
    $input.on("input", function () {
      $clearInput.toggle(!!this.value);
    })

    $clearInput.on('click', function () {
      $input.val("");
      $valueField.val("");
      $clearInput.toggle(false);
      $input.focus();
    })
  }
});
