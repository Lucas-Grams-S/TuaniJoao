/**
 * LÓGICA DE APRESENTAÇÃO (MOCK) PARA AVALIAÇÃO DOS NOIVOS
 * Substituiremos isso pelas chamadas reais da API (fetch) na próxima fase.
 */

function buscarFamiliaVisual() {
    const nomeBusca = document.getElementById('searchGuestName').value.trim();
    const feedback = document.getElementById('searchFeedback');
    const container = document.getElementById('familyContainer');
    const lista = document.getElementById('familyMembersList');

    // Esconde mensagens antigas
    feedback.classList.add('d-none');
    container.classList.add('d-none');

    if (nomeBusca.length < 3) {
        feedback.innerText = "Por favor, digite pelo menos 3 letras do seu nome.";
        feedback.classList.remove('d-none');
        return;
    }

    // SIMULAÇÃO: Dados falsos que viriam do Banco de Dados baseados no Lote da Família
    const familiaSimulada = [
        { id: 1, nome: nomeBusca, confirmado: true },
        { id: 2, nome: "Acompanhante 1", confirmado: false },
        { id: 3, nome: "Acompanhante 2 (Criança)", confirmado: false }
    ];

    // Limpa a lista atual
    lista.innerHTML = '';

    // Desenha os checkboxes na tela com as cores da paleta
    familiaSimulada.forEach(convidado => {
        const checkedStr = convidado.confirmado ? "checked" : "";
        const html = `
            <div class="form-check form-switch mb-3 p-3 border rounded" style="background-color: var(--bg-light); border-color: var(--sage-green) !important;">
                <input class="form-check-input ms-0 me-3" type="checkbox" role="switch" id="convidado_${convidado.id}" ${checkedStr} style="transform: scale(1.3); cursor: pointer;">
                <label class="form-check-label fw-bold" for="convidado_${convidado.id}" style="cursor: pointer; padding-top: 2px;">
                    ${convidado.nome}
                </label>
            </div>
        `;
        lista.innerHTML += html;
    });

    // Exibe o painel da família
    container.classList.remove('d-none');
}

function salvarRsvpVisual() {
    const btn = document.getElementById('btnSalvarRsvp');
    btn.innerHTML = "⏳ Salvando...";
    btn.disabled = true;

    // Simula o tempo de salvamento no banco de dados (1,5 segundos)
    setTimeout(() => {
        btn.innerHTML = "Salvar Confirmações";
        btn.disabled = false;

        // Exibe o modal bonitinho de sucesso
        const modal = new bootstrap.Modal(document.getElementById('modalSucessoRsvp'));
        modal.show();

        // Limpa a tela após fechar o modal
        document.getElementById('searchGuestName').value = '';
        document.getElementById('familyContainer').classList.add('d-none');
    }, 1500);
}