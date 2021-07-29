import 'babel-polyfill';
import React from 'react';
import { shallow } from 'enzyme';
import ModalTooltip from "../ModalTooltip";

describe('Diagram', () => {
	it('renders correctly', () => {
		const wrapper = shallow(
			<ModalTooltip
				target="test_id"
				header="test header"
				body="test body"
				placement="left"
				trigger="hover"
			/>);
			expect(wrapper).toMatchSnapshot();
	});
});

