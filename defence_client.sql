INSERT INTO public.defence_client(
            id, first_name, last_name, date_of_birth, defence_case_id)
    VALUES ('ac5eb804-42af-4239-b41c-2449857f4833', 'Joe', 'Bloggs', '1983-04-20', 'ac5eb804-42af-4239-b41c-2449857f4834'),
    ('ac5eb804-42af-4239-b41c-2449857f4837', 'Jimmy', 'Trousers', '1983-03-20', 'ac5eb804-42af-4239-b41c-2449857f4836');
INSERT INTO public.defence_case(defence_case_id, pti_urn, prosecution_authority)
VALUES ('ac5eb804-42af-4239-b41c-2449857f4834', 'URN001', 'Mars Planet Authority'),
('ac5eb804-42af-4239-b41c-2449857f4836', 'URN002', 'Mars Planet Authority');