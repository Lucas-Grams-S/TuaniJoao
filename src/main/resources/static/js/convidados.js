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
// ==========================================
// MÓDULO DE PAGAMENTO: PIX E CARTÃO (PÚBLICO)
// ==========================================

// Inicializa o Mercado Pago
const mp = new MercadoPago('TEST-83921b7f-5e2a-40f2-b7f7-44ab671f6299', {
    locale: 'pt-BR'
});
const bricksBuilder = mp.bricks();
let cardPaymentBrickController;

// 1. Função que abre as opções de pagamento
// No botão do HTML, vamos chamar esta função para o convidado escolher como quer pagar.
function abrirOpcoesDePagamento(giftId, price) {
    // Pergunta simples para separar o fluxo
    if (confirm("Deseja pagar via PIX? (Clique em OK para Pix, ou Cancelar para Cartão de Crédito)")) {
        abrirModalPix(giftId);
    } else {
        abrirModalCartao(giftId, price);
    }
}

// ---------------- FLUXO DO PIX ----------------
function abrirModalPix(giftId) {
    document.getElementById('modalGiftId').value = giftId;
    document.getElementById('guestName').value = '';
    document.getElementById('guestEmail').value = '';
    document.getElementById('guestCpf').value = '';
    document.getElementById('guestMessage').value = '';

    const modal = new bootstrap.Modal(document.getElementById('dadosConvidadoModal'));
    modal.show();
}

function confirmarDadosEGerarPix() {
    const giftId = document.getElementById('modalGiftId').value;
    const name = document.getElementById('guestName').value.trim();
    const email = document.getElementById('guestEmail').value.trim();
    const cpf = document.getElementById('guestCpf').value.trim();
    const message = document.getElementById('guestMessage').value.trim();

    if (!name || !email || !cpf) {
        alert("Por favor, preencha Nome, E-mail e CPF para prosseguir.");
        return;
    }

    const btnElement = document.getElementById('btnConfirmarPagamento');
    const textoOriginal = btnElement.innerHTML;
    btnElement.innerHTML = '⏳ Gerando Pix...';
    btnElement.disabled = true;

    const payload = { name, email, cpf, message };

    fetch(`/api/payments/pix/${giftId}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    })
    .then(async response => {
        const data = await response.json();
        if (!response.ok) throw new Error(data.message || 'Erro crítico ao gerar o Pix.');
        return data;
    })
    .then(data => {
        bootstrap.Modal.getInstance(document.getElementById('dadosConvidadoModal')).hide();
        document.getElementById('pixQrCodeImg').src = 'data:image/png;base64,' + data.qrCodeBase64;
        document.getElementById('pixCopiaCola').value = data.qrCodeCopiaECola;

        const pixModal = new bootstrap.Modal(document.getElementById('pixModal'));
        pixModal.show();
    })
    .catch(error => {
        alert('Falha na comunicação com o Mercado Pago: ' + error.message);
    })
    .finally(() => {
        btnElement.innerHTML = textoOriginal;
        btnElement.disabled = false;
    });
}

function copiarPix() {
    const inputPix = document.getElementById('pixCopiaCola');
    inputPix.select();
    inputPix.setSelectionRange(0, 99999);
    navigator.clipboard.writeText(inputPix.value);
    alert("Copiado com sucesso!");
}

// ---------------- FLUXO DO CARTÃO ----------------
function abrirModalCartao(giftId, price) {
    document.getElementById('modalCartaoGiftId').value = giftId;
    document.getElementById('guestCartaoMessage').value = '';

    const modal = new bootstrap.Modal(document.getElementById('modalCartao'));
    modal.show();

    renderCardPaymentBrick(price);
}

const renderCardPaymentBrick = async (amount) => {
    const settings = {
        initialization: { amount: amount },
        callbacks: {
            onReady: () => console.log("Formulário de cartão carregado."),
            onSubmit: (cardFormData) => processarPagamentoCartao(cardFormData),
            onError: (error) => console.warn("Aviso Mercado Pago: ", error.message),
        },
    };

    if (cardPaymentBrickController) cardPaymentBrickController.unmount();

    cardPaymentBrickController = await bricksBuilder.create("cardPayment", "cardPaymentBrick_container", settings);
};

function processarPagamentoCartao(cardFormData) {
    return new Promise((resolve, reject) => {
        const giftId = document.getElementById('modalCartaoGiftId').value;
        const mensagem = document.getElementById('guestCartaoMessage').value.trim();

        const payload = {
            name: cardFormData.payer.first_name || "Convidado",
            email: cardFormData.payer.email,
            cpf: cardFormData.payer.identification.number,
            message: mensagem,
            token: cardFormData.token,
            paymentMethodId: cardFormData.payment_method_id,
            installments: cardFormData.installments
        };

        fetch(`/api/payments/credit-card/${giftId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        })
        .then(async response => {
            const data = await response.json();
            if (!response.ok) throw new Error(data.message || 'Falha ao processar pagamento.');

            if(data.status === "approved") {
                alert("🎉 Pagamento Aprovado! O ID da transação é: " + data.paymentId);
                bootstrap.Modal.getInstance(document.getElementById('modalCartao')).hide();
                resolve();
            } else {
                throw new Error("O status do pagamento é: " + data.statusDetail);
            }
        })
        .catch(error => {
            alert('Aviso: ' + error.message);
            reject();
        });
    });
}