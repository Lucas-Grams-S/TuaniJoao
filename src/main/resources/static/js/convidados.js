/**
 * LÓGICA DE APRESENTAÇÃO (MOCK) PARA AVALIAÇÃO DOS NOIVOS
 * Substituiremos isso pelas chamadas reais da API (fetch) na próxima fase.
 */

// ==========================================
// MÓDULO DE CONFIRMAÇÃO DE PRESENÇA (RSVP)
// ==========================================

let searchTimeout = null;

/**
 * Função acionada pelo evento oninput enquanto o usuário digita.
 * Utiliza Debounce para aguardar 400ms após a última tecla digitada.
 */
function onSearchInputChange() {
    const nomeBusca = document.getElementById('searchGuestName').value.trim();
    const feedback = document.getElementById('searchFeedback');
    const container = document.getElementById('familyContainer');

    // Se o usuário apagar o texto ou deixar menos de 3 letras, oculta os resultados
    if (nomeBusca.length < 3) {
        feedback.classList.add('d-none');
        container.classList.add('d-none');
        return;
    }

    // Cancela a busca anterior se o usuário ainda estiver digitando
    if (searchTimeout) {
        clearTimeout(searchTimeout);
    }

    // Agenda a busca para 400ms após a última tecla pressionada
    searchTimeout = setTimeout(() => {
        buscarFamiliaVisual();
    }, 400);
}

function buscarFamiliaVisual() {
    const nomeBusca = document.getElementById('searchGuestName').value.trim();
    const feedback = document.getElementById('searchFeedback');
    const container = document.getElementById('familyContainer');
    const lista = document.getElementById('familyMembersList');

    feedback.classList.add('d-none');
    container.classList.add('d-none');

    // Mínimo de caracteres para evitar buscar a lista inteira com uma única letra
    if (nomeBusca.length < 3) {
        feedback.innerText = "Por favor, digite pelo menos 3 letras do seu nome.";
        feedback.classList.remove('d-none');
        return;
    }

    fetch(`/api/guests/search?name=${encodeURIComponent(nomeBusca)}`)
        .then(async response => {
            if (!response.ok) throw new Error('Erro ao buscar convidados.');
            return response.json();
        })
        .then(convidados => {
            lista.innerHTML = '';

            if (!convidados || convidados.length === 0) {
                feedback.innerText = "Nenhum convite encontrado com esse nome. Verifique a ortografia ou tente apenas o primeiro nome.";
                feedback.classList.remove('d-none');
                return;
            }

            // Desenha os checkboxes na tela
            convidados.forEach(convidado => {
                const checkedStr = convidado.isConfirmed ? "checked" : "";
                const html = `
                    <div class="form-check form-switch mb-3 p-3 border rounded" style="background-color: var(--bg-light); border-color: var(--sage-green) !important;">
                        <input class="form-check-input ms-0 me-3 rsvp-checkbox"
                               type="checkbox"
                               role="switch"
                               id="convidado_${convidado.id}"
                               value="${convidado.id}"
                               ${checkedStr}
                               style="transform: scale(1.3); cursor: pointer;">
                        <label class="form-check-label fw-bold" for="convidado_${convidado.id}" style="cursor: pointer; padding-top: 2px;">
                            ${convidado.name}
                        </label>
                    </div>
                `;
                lista.innerHTML += html;
            });

            container.classList.remove('d-none');
        })
        .catch(error => {
            feedback.innerText = "Ocorreu um erro ao comunicar com o servidor. Tente novamente.";
            feedback.classList.remove('d-none');
        });
}

function salvarRsvpVisual() {
    const btn = document.getElementById('btnSalvarRsvp');
    btn.innerHTML = "⏳ Salvando...";
    btn.disabled = true;

    const checkboxes = document.querySelectorAll('.rsvp-checkbox:checked');
    const idsConfirmados = Array.from(checkboxes).map(cb => parseInt(cb.value));

    if (idsConfirmados.length === 0) {
        alert("Por favor, marque pelo menos um nome para confirmar presença.");
        btn.innerHTML = "Salvar Confirmações";
        btn.disabled = false;
        return;
    }

    fetch('/api/guests/confirm', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(idsConfirmados)
    })
    .then(response => {
        if (!response.ok) throw new Error('Falha ao registrar confirmação.');

        const modal = new bootstrap.Modal(document.getElementById('modalSucessoRsvp'));
        modal.show();

        document.getElementById('searchGuestName').value = '';
        document.getElementById('familyContainer').classList.add('d-none');
    })
    .catch(error => {
        alert('Aviso: ' + error.message);
    })
    .finally(() => {
        btn.innerHTML = "Salvar Confirmações";
        btn.disabled = false;
    });
}
// ==========================================
// MÓDULO DE PAGAMENTO: PIX E CARTÃO (PÚBLICO)
// ==========================================

// Inicializa o Mercado Pago
const mp = new MercadoPago('APP_USR-8e5905b4-85c5-42c9-82bc-2bd4f12cec59', {
    locale: 'pt-BR'
});
const bricksBuilder = mp.bricks();
let cardPaymentBrickController;

// 1. Função que abre as opções de pagamento
// No botão do HTML, vamos chamar esta função para o convidado escolher como quer pagar.
// 1. Abre o modal bonito de escolha e guarda os valores
function abrirOpcoesDePagamento(giftId, price) {
    document.getElementById('escolhaGiftId').value = giftId;
    document.getElementById('escolhaGiftPrice').value = price;

    const modal = new bootstrap.Modal(document.getElementById('modalEscolhaPagamento'));
    modal.show();
}

// 2. Se o usuário escolher PIX
function escolherPix() {
    const giftId = document.getElementById('escolhaGiftId').value;

    // Esconde o modal de escolha
    bootstrap.Modal.getInstance(document.getElementById('modalEscolhaPagamento')).hide();

    // Abre o fluxo do Pix
    abrirModalPix(giftId);
}

// 3. Se o usuário escolher CARTÃO
function escolherCartao() {
    const giftId = document.getElementById('escolhaGiftId').value;
    const price = document.getElementById('escolhaGiftPrice').value;

    // Esconde o modal de escolha
    bootstrap.Modal.getInstance(document.getElementById('modalEscolhaPagamento')).hide();

    // Abre o fluxo do Cartão
    abrirModalCartao(giftId, price);
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