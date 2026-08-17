let stagedGifts = [];

// Função global para gerar alertas flutuantes bonitos em QUALQUER página
function showStylishAlert(message, type = 'success') {
    let alertContainer = document.getElementById('global-alert-container');
    if (!alertContainer) {
        alertContainer = document.createElement('div');
        alertContainer.id = 'global-alert-container';
        alertContainer.style.cssText = 'position: fixed; top: 20px; right: 20px; z-index: 9999; min-width: 300px; max-width: 400px;';
        document.body.appendChild(alertContainer);
    }

    const alertEl = document.createElement('div');
    alertEl.className = `alert alert-${type} alert-dismissible fade show shadow-lg`;
    alertEl.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;
    alertContainer.appendChild(alertEl);

    // Auto-destruição do alerta após 5 segundos
    setTimeout(() => {
        alertEl.classList.remove('show');
        setTimeout(() => alertEl.remove(), 150);
    }, 5000);
}

function uploadPhoto(input) {
    const file = input.files[0];
    if (!file) return;

    const spinner = document.getElementById('uploadSpinner');
    const successText = document.getElementById('uploadSuccess');
    const btnAdd = document.getElementById('btnAddToList');

    if(spinner) spinner.classList.remove('d-none');
    if(successText) successText.classList.add('d-none');
    if(btnAdd) btnAdd.disabled = true;

    const formData = new FormData();
    formData.append('file', file);

    fetch('/api/gifts/photo', {
        method: 'POST',
        body: formData
    })
    .then(response => {
        if (!response.ok) throw new Error('Falha no upload');
        return response.text();
    })
    .then(photoUrl => {
        document.getElementById('giftPhotoUrl').value = photoUrl;
        if(spinner) spinner.classList.add('d-none');
        if(successText) successText.classList.remove('d-none');
        if(btnAdd) btnAdd.disabled = false;
    })
    .catch(error => {
        if(spinner) spinner.classList.add('d-none');
        showStylishAlert('Erro ao fazer upload: ' + error.message, 'danger');
    });
}

function addGiftToList(event) {
    event.preventDefault();
    const name = document.getElementById('giftName').value;
    const price = document.getElementById('giftPrice').value;
    const description = document.getElementById('giftDescription').value;
    const photoUrl = document.getElementById('giftPhotoUrl').value;

    const gift = { name, price: parseFloat(price), description, photoUrl };
    stagedGifts.push(gift);

    document.getElementById('giftForm').reset();
    const successText = document.getElementById('uploadSuccess');
    if(successText) successText.classList.add('d-none');

    document.getElementById('btnAddToList').disabled = true;
    renderTable();
}

function removeGiftFromList(index) {
    stagedGifts.splice(index, 1);
    renderTable();
}

function renderTable() {
    const tbody = document.getElementById('giftsTableBody');
    if(!tbody) return;

    tbody.innerHTML = '';
    if (stagedGifts.length === 0) {
        tbody.innerHTML = `<tr id="emptyRow"><td colspan="5" class="text-center text-muted py-4">Nenhum presente adicionado ao lote ainda.</td></tr>`;
        document.getElementById('btnSaveBatch').disabled = true;
        document.getElementById('batchCount').innerText = '0';
        return;
    }

    stagedGifts.forEach((gift, index) => {
        const row = `
            <tr>
                <td><img src="${gift.photoUrl}" class="preview-img" alt="Preview" style="width:50px; height:50px; object-fit:cover; border-radius:4px;"></td>
                <td><strong>${gift.name}</strong></td>
                <td>R$ ${gift.price.toFixed(2)}</td>
                <td class="text-truncate" style="max-width: 200px;">${gift.description || '-'}</td>
                <td class="text-end">
                    <button class="btn btn-outline-danger btn-sm" onclick="removeGiftFromList(${index})">✕ Retirar</button>
                </td>
            </tr>`;
        tbody.innerHTML += row;
    });

    document.getElementById('btnSaveBatch').disabled = false;
    document.getElementById('batchCount').innerText = stagedGifts.length;
}

function saveGiftsBatch() {
    const btnSave = document.getElementById('btnSaveBatch');
    const batchCountSpan = document.getElementById('batchCount');

    btnSave.disabled = true;
    batchCountSpan.innerText = '⏳';

    fetch('/api/gifts/batch', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(stagedGifts)
    })
    .then(async response => {
        if (!response.ok) throw new Error('Falha no servidor.');
        return response.json();
    })
    .then(data => {
        const qtd = data ? data.length : stagedGifts.length;
        showStylishAlert(`🎉 <strong>Sucesso!</strong> ${qtd} presentes salvos no catálogo.`, 'success');
        stagedGifts = [];
        renderTable();
    })
    .catch(error => {
        showStylishAlert('Aviso: ' + error.message, 'warning');
        btnSave.disabled = false;
        renderTable();
    });
}

function uploadCsvGuests() {
    const fileInput = document.getElementById('csvFile');
    if(!fileInput) return;
    const file = fileInput.files[0];

    if (!file) {
        showStylishAlert('Selecione um arquivo .csv primeiro.', 'warning');
        return;
    }
    const formData = new FormData();
    formData.append('file', file);
    showStylishAlert('⏳ Processando arquivo...', 'info');

    fetch('/api/guests/upload-csv', {
        method: 'POST',
        body: formData
    })
    .then(async response => {
        const text = await response.text();
        if (!response.ok) throw new Error(text || 'Falha no upload.');
        return text;
    })
    .then(message => {
        showStylishAlert('🎉 ' + message, 'success');
        fileInput.value = '';
    })
    .catch(error => showStylishAlert('Erro: ' + error.message, 'danger'));
}

function abrirModalDadosPessoais(giftId) {
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
        showStylishAlert("⚠️ Por favor, preencha Nome, E-mail e CPF para prosseguir.", "warning");
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
        showStylishAlert('Falha na comunicação com o Mercado Pago: ' + error.message, 'danger');
    })
    .finally(() => {
        btnElement.innerHTML = textoOriginal;
        btnElement.disabled = false;
    });
}

let mp = null;
let bricksBuilder = null;

if (typeof MercadoPago !== 'undefined') {
    mp = new MercadoPago('TEST-83921b7f-5e2a-40f2-b7f7-44ab671f6299', {
        locale: 'pt-BR'
    });
    bricksBuilder = mp.bricks();
}

let cardPaymentBrickController;

function abrirModalCartao(giftId, price) {
    document.getElementById('modalCartaoGiftId').value = giftId;
    document.getElementById('guestCartaoMessage').value = '';

    const modal = new bootstrap.Modal(document.getElementById('modalCartao'));
    modal.show();

    renderCardPaymentBrick(price);
}

const renderCardPaymentBrick = async (amount) => {
    if (!bricksBuilder) return;

    const settings = {
        initialization: { amount: amount },
        callbacks: {
            onReady: () => { console.log("Formulário de cartão carregado."); },
            onSubmit: (cardFormData) => { return processarPagamentoCartao(cardFormData); },
            onError: (error) => { console.warn("Aviso interno do Mercado Pago: ", error.message); },
        },
    };

    if (cardPaymentBrickController) {
        cardPaymentBrickController.unmount();
    }

    cardPaymentBrickController = await bricksBuilder.create("cardPayment", "cardPaymentBrick_container", settings);
};

function processarPagamentoCartao(cardFormData) {
    return new Promise((resolve, reject) => {
        const giftId = document.getElementById('modalCartaoGiftId').value;
        const mensagem = document.getElementById('guestCartaoMessage').value.trim();

        const payload = {
            name: cardFormData.payer.first_name || "Convidado Teste",
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
                showStylishAlert("🎉 Pagamento Aprovado com Sucesso! O ID da transação é: " + data.paymentId, "success");
                bootstrap.Modal.getInstance(document.getElementById('modalCartao')).hide();
                resolve();
            } else {
                throw new Error("O pagamento foi registado, mas o status é: " + data.statusDetail);
            }
        })
        .catch(error => {
            showStylishAlert('Aviso: ' + error.message, 'warning');
            reject();
        });
    });
}

function copiarPix() {
    const inputPix = document.getElementById('pixCopiaCola');
    inputPix.select();
    inputPix.setSelectionRange(0, 99999);
    navigator.clipboard.writeText(inputPix.value);
    showStylishAlert("📋 Código Pix copiado com sucesso!", "info");
}

function abrirModalExcluirConvidado(id, name) {
    document.getElementById('deleteGuestId').value = id;
    document.getElementById('deleteGuestName').innerText = name;

    const modal = new bootstrap.Modal(document.getElementById('modalExcluirConvidado'));
    modal.show();
}

function confirmarExclusaoConvidado() {
    const id = document.getElementById('deleteGuestId').value;
    const btnExcluir = document.getElementById('btnConfirmarExclusao');

    btnExcluir.disabled = true;
    btnExcluir.innerText = 'Excluindo...';

    fetch(`/api/guests/${id}`, { method: 'DELETE' })
    .then(response => {
        if (!response.ok) throw new Error('Falha ao excluir o convidado.');
        window.location.reload();
    })
    .catch(error => {
        showStylishAlert('Aviso: ' + error.message, 'danger');
        btnExcluir.disabled = false;
        btnExcluir.innerText = 'Sim, Excluir';
    });
}

async function excluirPresente(id, botao) {
    if (!confirm('Tem certeza que deseja excluir este presente da lista pública do casamento?')) {
        return;
    }

    botao.disabled = true;
    botao.textContent = '⏳...';

    try {
        const resposta = await fetch(`/api/gifts/${id}`, { method: 'DELETE' });

        if (resposta.ok || resposta.status === 204) {
            const linha = botao.closest('tr');
            linha.style.transition = 'opacity 0.4s ease';
            linha.style.opacity = '0';

            setTimeout(() => {
                linha.remove();
                const tbody = document.querySelector('table tbody');
                if (tbody && tbody.children.length === 0) {
                    location.reload();
                }
            }, 400);

            showStylishAlert('🗑️ Presente removido com sucesso!', 'info');
        } else {
            const erroTxt = await resposta.text();
            throw new Error(erroTxt || 'Erro interno ao tentar excluir.');
        }
    } catch (erro) {
        showStylishAlert('Não foi possível excluir o presente: ' + erro.message, 'danger');
        botao.disabled = false;
        botao.textContent = '🗑️ Excluir';
    }
}