let stagedGifts = [];

function uploadPhoto(input) {
    const file = input.files[0];
    if (!file) return;

    const spinner = document.getElementById('uploadSpinner');
    const successText = document.getElementById('uploadSuccess');
    const btnAdd = document.getElementById('btnAddToList');

    spinner.classList.remove('d-none');
    successText.classList.add('d-none');
    btnAdd.disabled = true;

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
        spinner.classList.add('d-none');
        successText.classList.remove('d-none');
        btnAdd.disabled = false;
    })
    .catch(error => {
        spinner.classList.add('d-none');
        showGiftAlert('Erro ao fazer upload: ' + error.message, 'alert-danger');
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
    document.getElementById('uploadSuccess').classList.add('d-none');
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
                <td><img src="${gift.photoUrl}" class="preview-img" alt="Preview" style="width:50px; height:50px; object-fit:cover;"></td>
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
        if (!response.ok) throw new Error('Falha no servidor ao salvar lote.');
        return response.json();
    })
    .then(data => {
        const qtd = data ? data.length : stagedGifts.length;
        showGiftAlert(`🎉 <strong>Sucesso!</strong> ${qtd} presente(s) salvo(s) com sucesso na lista do casamento.`, 'alert-success');
        stagedGifts = [];
        renderTable();
    })
    .catch(error => {
        showGiftAlert('Aviso: ' + error.message, 'alert-danger');
        btnSave.disabled = false;
        renderTable();
    });
}

function showGiftAlert(message, bootstrapClass) {
    const alertBox = document.getElementById('giftAlert');
    if(!alertBox) return;
    alertBox.className = `alert ${bootstrapClass} mt-3 shadow-sm`;
    alertBox.innerHTML = message;
    alertBox.classList.remove('d-none');
}

function uploadCsvGuests() {
    const fileInput = document.getElementById('csvFile');
    const file = fileInput.files[0];
    if (!file) {
        showAlert('Selecione um arquivo.', 'alert-danger');
        return;
    }
    const formData = new FormData();
    formData.append('file', file);
    showAlert('Processando...', 'alert-info');

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
        showAlert('🎉 ' + message, 'alert-success');
        fileInput.value = '';
    })
    .catch(error => showAlert('Erro: ' + error.message, 'alert-danger'));
}

function showAlert(message, bootstrapClass) {
    const alertBox = document.getElementById('csvAlert');
    if(!alertBox) return;
    alertBox.className = `alert ${bootstrapClass} mt-3 shadow-sm`;
    alertBox.innerHTML = message;
    alertBox.classList.remove('d-none');
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

// Configuração segura do Mercado Pago (Só inicializa se a biblioteca estiver presente na página)
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
        initialization: {
            amount: amount,
        },
        callbacks: {
            onReady: () => {
                console.log("Formulário de cartão carregado.");
            },
            onSubmit: (cardFormData) => {
                return processarPagamentoCartao(cardFormData);
            },
            onError: (error) => {
                console.warn("Aviso interno do Mercado Pago: ", error.message);
            },
        },
    };

    if (cardPaymentBrickController) {
        cardPaymentBrickController.unmount();
    }

    cardPaymentBrickController = await bricksBuilder.create(
        "cardPayment",
        "cardPaymentBrick_container",
        settings
    );
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
                alert("🎉 Pagamento Aprovado com Sucesso! O ID da transação é: " + data.paymentId);
                bootstrap.Modal.getInstance(document.getElementById('modalCartao')).hide();
                resolve();
            } else {
                throw new Error("O pagamento foi registado, mas o status é: " + data.statusDetail);
            }
        })
        .catch(error => {
            alert('Aviso: ' + error.message);
            reject();
        });
    });
}

function copiarPix() {
    const inputPix = document.getElementById('pixCopiaCola');
    inputPix.select();
    inputPix.setSelectionRange(0, 99999);
    navigator.clipboard.writeText(inputPix.value);
    alert("Copiado com sucesso!");
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

    fetch(`/api/guests/${id}`, {
        method: 'DELETE'
    })
    .then(response => {
        if (!response.ok) throw new Error('Falha ao excluir o convidado.');

        window.location.reload();
    })
    .catch(error => {
        alert('Aviso: ' + error.message);
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
        const resposta = await fetch(`/api/gifts/${id}`, {
            method: 'DELETE'
        });

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

        } else {
            const erroTxt = await resposta.text();
            throw new Error(erroTxt || 'Erro interno ao tentar excluir.');
        }
    } catch (erro) {
        alert('Não foi possível excluir o presente: ' + erro.message);
        botao.disabled = false;
        botao.textContent = '🗑️ Excluir';
    }
}