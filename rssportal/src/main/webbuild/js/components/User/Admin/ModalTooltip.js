import React from "react";
import {UncontrolledPopover, PopoverHeader, PopoverBody} from "reactstrap";

const ModalTooltip = ({target, header, body, placement, trigger}) => {
	return (
		<UncontrolledPopover
			placement={placement}
			target={target}
			className="admin-system-modal"
			trigger={trigger}
			hideArrow={true}
			fade={false}
		>
			<PopoverHeader>{header}</PopoverHeader>
			<PopoverBody>{body}</PopoverBody>
		</UncontrolledPopover>
	);
}

export default ModalTooltip;