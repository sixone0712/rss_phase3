import React from "react";
import {faCircle} from "@fortawesome/free-solid-svg-icons";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";

const CheckBox = ({index, name, isChecked, labelClass, handleCheckboxClick, status, auto}) => {
    if (name === null || index === null) {
      return null;
    } else {
        const labelName = labelClass === "filelist-label" ? "" : name;
        const setChecked = isChecked === true;
        let isShowMachineStatus = false;
        let isMachineRunning = false;
        let isDisable = false;

        if(status !== undefined) {
            isShowMachineStatus = true;
            isMachineRunning = status;
            if(auto === undefined) {
                isDisable = !status;
            }
        } else {
            isShowMachineStatus = false;
            isMachineRunning = true;
        }

        return (
            <>
              <input
                  disabled={isDisable}
                  type="checkbox"
                  className="custom-control-input"
                  id={name + "_{#div#}_" + index}
                  value={name}
                  checked={setChecked}
                  onChange={handleCheckboxClick}
              />

              <label
                  className={"custom-control-label " + labelClass}
                  htmlFor={name + "_{#div#}_" + index}
              >
                  { isShowMachineStatus &&
                    <FontAwesomeIcon className={isMachineRunning ? "machine-status-running" : "machine-status-unknown"} icon={faCircle} />
                  }
                  {labelName}
              </label>

          </>
        );
    }
}

export default CheckBox;
