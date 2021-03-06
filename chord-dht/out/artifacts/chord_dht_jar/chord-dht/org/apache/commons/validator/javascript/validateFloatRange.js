
   /*$RCSfile: validateFloatRange.js,v $ $Rev: 232626 $ $Date: 2005-08-14 18:45:34 +0100 (Sun, 14 Aug 2005) $ */
    /**
    * Check to see if fields are in a valid float range.
    * Fields are not checked if they are disabled.
    * <p>
    * @param form The form validation is taking place on.
    */
    function validateFloatRange(form) {
        var isValid = true;
        var focusField = null;
        var i = 0;
        var fields = new Array();
        
        oRange = eval('new ' + retrieveFormName(form) +  '_floatRange()');
        for (x in oRange) {
            var field = form[oRange[x][0]];
            
            if ((field.type == 'hidden' ||
                field.type == 'text' || field.type == 'textarea') &&
                (field.value.length > 0)  &&
                 field.disabled == false) {
        
                var fMin = parseFloat(oRange[x][2]("min"));
                var fMax = parseFloat(oRange[x][2]("max"));
                var fValue = parseFloat(field.value);
                if (!(fValue >= fMin && fValue <= fMax)) {
                    if (i == 0) {
                        focusField = field;
                    }
                    fields[i++] = oRange[x][1];
                    isValid = false;
                }
            }
        }
        if (fields.length > 0) {
            focusField.focus();
            alert(fields.join('\n'));
        }
        return isValid;
    }
